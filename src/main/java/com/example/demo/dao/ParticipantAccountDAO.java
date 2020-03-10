package com.example.demo.dao;

import com.example.demo.model.ParticipantAccount;
import com.example.demo.model.ParticipantAccountLocation;
import com.example.demo.service.ParticipantAccountMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class ParticipantAccountDAO {

    private static final Logger LOG = LoggerFactory.getLogger(ParticipantAccountDAO.class);
    private static final String PARAM_NAME_PARTICIPANT_ACC_ID = "participantAccId";
    private static final String PARAM_NAME_PARTICIPANT_ACC_IDS = "participantAccIds";
    private static final String PARAM_NAME_PARTICIPANT_IDS = "participantIds";
    private static final String PARAM_NAME_PARTICIPANT_EXTERNAL_ID = "participantExternalId";
    private static final String PARAM_NAME_PARTICIPANT_ACCOUNT_EXTERNAL_ID = "participantAccountExternalId";
    private static final String PARAM_NAME_CONTACT_ID = "contactId";
    private static final String PARAM_NAME_LOCATION_ID = "locationId";
    private static final String PARAM_NAME_CONTACT_TYPE = "contactType";
    private static final String PARAM_NAME_LOCATION_TYPE = "locationType";
    private static final String PARAM_NAME_ACCOUNT_ROLE_ID = "roleId";
    private static final String PARAM_NAME_PARTICIPANT_NAME = "name";
    private static final String PARAM_NAME_ACTIVE = "active";
    private static final String PARAM_NAME_CHARGE_VAT = "chargeVat";
    private static final String PARAM_NAME_ACCOUNT_NAME = "accountName";
    private static final String PARAM_NAME_PARTICIPANT_ID = "participantId";
    private static final String PARAM_NAME_ORGANIZATION_NO = "organisationNo";
    private static final String PARAM_NAME_DEFAULT_ACC = "defaultAccount";
    private static final String PARAM_NAME_CREATED_TIME = "createdTime";
    private static final String PARAM_NAME_CREATED_BY = "createdBy";
    private static final String PARAM_NAME_UPDATED_TIME = "updatedTime";
    private static final String PARAM_NAME_UPDATED_BY = "lastUpdatedBy";
    private static final String PARAM_NAME_SHORT_NAME = "shortName";
    private static final String PARAM_NAME_COLLECTION_POINT_ID = "collectionPointId";
    private static final String PARAM_NAME_INCOTERM_CODE_ID = "incotermCode";
    private static final String PARAM_NAME_CATEGORY_ID = "categoryId";
    private static final String PARAM_NAME_DAY = "day";
    private static final String PARAM_NAME_OPENING_TIME = "openingTime";
    private static final String PARAM_NAME_CLOSING_TIME = "closingTime";
    private static final String PARAM_NAME_GLN = "gln";
    private static final String PARAM_NAME_LATITUDE = "latitiude";
    private static final String PARAM_NAME_LONGITUDE = "longitude";
    private static final String PARAM_NAME_USER = "user";

    private final LocationDAO locationDAO;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;


    //    @Audit(
//        schemaName = "mdm",
//        tableName = "participant_account",
//        columnNames = {"account_name"},
//        selectionColumnNames = {"participant_account_db_id"},
//        selectionValueNames = {"participantAccountId"},
//        pkColumnName = "participant_account_db_id"
//    )
    public boolean updateParticipantAccount(long participantAccountId, ParticipantAccount participantAccount) {
        try {
            StringBuilder sql = new StringBuilder("update mdm.participant_account set ");
            MapSqlParameterSource parameters = new MapSqlParameterSource();

            if (participantAccount.getAccountName() != null) {
                sql.append("account_name = :" + PARAM_NAME_ACCOUNT_NAME);
                parameters.addValue(PARAM_NAME_ACCOUNT_NAME, participantAccount.getAccountName());
            }

            sql.append(" where participant_account_db_id = :participantAccountId");
            parameters.addValue("participantAccountId", participantAccountId);

            return namedJdbcTemplate.update(sql.toString(), parameters) == 1;
        } catch (DataAccessException ex) {
            LOG.error("Error occurred while updating Participant Account. {0}", ex);
            return false;
        }
    }


    public boolean deleteParticipantAccLocations(Long participantAccountId, List<Long> idsToBeDeleted) { //might have to change the query

        String sql = "delete from mdm.participant_account_location " +
                "where fk_participant_account_id = :" + PARAM_NAME_PARTICIPANT_ACC_ID +
                " AND location_id = :location_id";

        int count = 0;
        for (Long idTobeDeleted : idsToBeDeleted) {

            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue(PARAM_NAME_PARTICIPANT_ACC_ID, participantAccountId);
            parameters.addValue("location_id", idTobeDeleted);
            count = count + namedJdbcTemplate.update(sql, parameters);
        }

        return count > 0;
    }

    public boolean createParticipantAccountLocations(long participantAccId,
                                                     List<ParticipantAccountLocation> locationList) {

        if (CollectionUtils.isEmpty(locationList)) {
            return false;
        }

        String locationCreateSql = "insert into mdm.participant_account_location" +
                "(fk_participant_account_id, location_id, location_name) values (" +
                ":" + "fk_participant_account_id" + "," +
                ":" + "location_id" + "," +
                ":" + "location_name" +
                ")";

        SqlParameterSource[] contactParameterBatch = new SqlParameterSource[locationList.size()];
        for (int i = 0; i < contactParameterBatch.length; i++) {
            contactParameterBatch[i] = buildLocationParamSource(participantAccId, locationList.get(i));
        }
        int[] rowCount = namedJdbcTemplate.batchUpdate(locationCreateSql, contactParameterBatch);
        return rowCount != null && rowCount.length == locationList.size();
    }

    public boolean updateParticipantAccountLocations(long participantAccId, List<ParticipantAccountLocation> locationsToUpdate) {

        if (CollectionUtils.isEmpty(locationsToUpdate)) {
            return false;
        }

        String locationCreateSql = "update mdm.participant_account_location" +
                " set location_name = :location_name where " +
                "" + "fk_participant_account_id = :fk_participant_account_id AND location_id = :location_id";

        SqlParameterSource[] contactParameterBatch = new SqlParameterSource[locationsToUpdate.size()];
        for (int i = 0; i < contactParameterBatch.length; i++) {
            contactParameterBatch[i] = buildLocationParamSource(participantAccId, locationsToUpdate.get(i));
        }
        int[] rowCount = namedJdbcTemplate.batchUpdate(locationCreateSql, contactParameterBatch);
        return rowCount != null && rowCount.length == locationsToUpdate.size();
    }

    private SqlParameterSource buildLocationParamSource(long participantAccId, ParticipantAccountLocation location) {
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource();
        mapSqlParameterSource.addValue("fk_participant_account_id", participantAccId);
        mapSqlParameterSource.addValue("location_id", location.getLocationId());
        mapSqlParameterSource.addValue("location_name", location.getLocationName());

        return mapSqlParameterSource;
    }

    public ParticipantAccount getParticipantAccountById(long participantAccountId) {
        return getParticipantAccountByIdWithoutAcquireLock(participantAccountId);
    }

    private ParticipantAccount getParticipantAccountByIdWithoutAcquireLock(long participantAccountId) {

        String sql = "select * from mdm.participant_account where participant_account_db_id = :"
                + PARAM_NAME_PARTICIPANT_ACC_ID;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue(PARAM_NAME_PARTICIPANT_ACC_ID, participantAccountId);
        return namedJdbcTemplate.queryForObject(sql, parameters, new ParticipantAccountMapper());
    }
}
