package com.example.demo.dao;

import com.example.demo.audit.deleteaudit.DeleteAudit;
import com.example.demo.model.Location;
import com.example.demo.model.ParticipantAccountLocation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Repository
@RequiredArgsConstructor
public class LocationDAO {

    private static final Logger LOG = LoggerFactory.getLogger(LocationDAO.class);

    private static final String COLUMN_NAME_LOCATION_DB_ID = "location_db_id";
    private static final String PARAM_NAME_PARTICIPANT_ACC_ID = "participantAccId";
    private static final String PARAM_NAME_LOCATION_TYPE = "locationType";
    private static final String PARAM_NAME_LOCATION_IDS = "locationIds";
    private static final String PARAM_NAME_ADDRESS_NAME_OVERRIDE = "addressNameOverride";
    private static final String PARAM_NAME_ADDRESS = "address";
    private static final String PARAM_NAME_ADDRESS2 = "address2";
    private static final String PARAM_NAME_CITY = "city";
    private static final String PARAM_NAME_POST_NUMBER = "postNumber";
    private static final String PARAM_NAME_POST_PLACE = "postPlace";
    private static final String PARAM_NAME_COUNTRY_CODE = "countryCode";
    private static final String PARAM_NAME_ACTIVE = "active";
    private static final String PARAM_NAME_PRIMARY_LOCATION = "primaryLocation";
    private static final String PARAM_NAME_CREATED = "created";
    private static final String PARAM_NAME_CREATED_BY = "createdBy";
    private static final String PARAM_NAME_UPDATED = "updated";
    private static final String PARAM_NAME_UPDATED_BY = "updatedBy";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<ParticipantAccountLocation> getLocationsOfParticipantAccounts(List<Long> participantAccountIds,
                                                                              boolean activeOnly,
                                                                              boolean primaryOnly,
                                                                              Long locationTypeId) {

        if (CollectionUtils.isEmpty(participantAccountIds)) {
            LOG.info("No locations are retrieved because no participant account IDs are provided.");
            return Collections.emptyList();
        }

        LOG.info("Querying locations for participant account [{}], activeOnly [{}], primaryOnly [{}], and type [{}]",
                participantAccountIds, activeOnly, primaryOnly, locationTypeId);

        StringBuilder sqlToSelectLocations = new StringBuilder()
                .append("select l.*, pal.fk_participant_account_id, pal.fk_location_type")
                .append(" from mdm.location l")
                .append(" join mdm.participant_account_location pal")
                .append(" on pal.fk_participant_account_id in(:" + PARAM_NAME_PARTICIPANT_ACC_ID + ")")
                .append(" and pal.fk_location_id = l.location_db_id");

        MapSqlParameterSource parameters = new MapSqlParameterSource(PARAM_NAME_PARTICIPANT_ACC_ID,
                participantAccountIds);

        if (activeOnly) {
            sqlToSelectLocations.append(" and l.active = true");
        }

        if (primaryOnly) {
            sqlToSelectLocations.append(" and l.primary_location = true");
        }

        if (locationTypeId != null) {
            sqlToSelectLocations.append(" and pal.fk_location_type in (:" + PARAM_NAME_LOCATION_TYPE + ")");
            parameters.addValue(PARAM_NAME_LOCATION_TYPE, locationTypeId);
        }

//        return namedParameterJdbcTemplate.query(sqlToSelectLocations.toString(), parameters,
//            new ParticipantAccountLocationMapper());

        return new ArrayList<>();
    }

    public List<ParticipantAccountLocation> getLocationsOfParticipantAccountWithRecordLock(Long participantAccountId) {
        LOG.info("Querying locations for participant account [{}]",
                participantAccountId);
        String sqlToSelectParticipantLocationId = "select pal.fk_location_id " +
                "from mdm.participant_account_location pal " +
                "where pal.fk_participant_account_id = :" + PARAM_NAME_PARTICIPANT_ACC_ID +
                " for update";

        MapSqlParameterSource parameters = new MapSqlParameterSource(
                PARAM_NAME_PARTICIPANT_ACC_ID,
                participantAccountId);

        List<Long> locationIds = namedParameterJdbcTemplate.queryForList(
                sqlToSelectParticipantLocationId,
                parameters,
                Long.class);

        if (locationIds.isEmpty()) {
            return Collections.emptyList();
        }

        String sqlToSelectLocations = "select l.*" +
                " from mdm.location l" +
                " where l.location_db_id in (:" + PARAM_NAME_LOCATION_IDS + ")" +
                " for update";

        MapSqlParameterSource locationParams = new MapSqlParameterSource(
                PARAM_NAME_LOCATION_IDS,
                locationIds
        );

        List<Location> locations = new ArrayList<>();

        return locations.stream().map(location -> {
            ParticipantAccountLocation participantAccountLocation = new ParticipantAccountLocation();
//            participantAccountLocation.setParticipantAccountId(participantAccountId);
            BeanUtils.copyProperties(location, participantAccountLocation);
            return participantAccountLocation;
        }).collect(Collectors.toList());
    }

    /**
     * Persists a given list of locations
     *
     * @return a list of generated IDs
     */
    public List<Long> createLocations(List<Location> locations) {

        String sql = "insert into mdm.location(" +
                "location_name) values(" +
                ":" + "location_name" +
                ")";

        List<Long> locationIds = new ArrayList<>(locations.size());

        locations.forEach(location -> {
            SqlParameterSource sqlParameterSource = buildSqlParamSourceFromLocation(location);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(sql, sqlParameterSource, keyHolder, new String[]{COLUMN_NAME_LOCATION_DB_ID});
            locationIds.add(keyHolder.getKey().longValue());
        });

        return locationIds;

    }

    private SqlParameterSource buildSqlParamSourceFromLocation(Location location) {

        LocalDateTime now = LocalDateTime.now();

        MapSqlParameterSource parameterMap = new MapSqlParameterSource();
        parameterMap.addValue("location_name", location.getLocationName());

//        parameterMap.addValue(PARAM_NAME_CREATED_BY, SecurityUtil.getUserName());
//        parameterMap.addValue(PARAM_NAME_UPDATED_BY, SecurityUtil.getUserName());

        return parameterMap;
    }

    @DeleteAudit(
            schemaName = "mdm",
            tableName = "location",
            columnNames = {},
            selectionColumnNames = {"location_db_id"},
            selectionValueNames = {"location_db_id"},
            pkColumnName = "location_db_id"
    )
    public boolean deleteLocations(List<Long> locationIds) {
        String sql = "delete from mdm.location " +
                "where location_db_id in (:" + PARAM_NAME_LOCATION_IDS + ")";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue(PARAM_NAME_LOCATION_IDS, locationIds);
        int count = namedParameterJdbcTemplate.update(sql, parameters);
        return count > 0;
    }
}
