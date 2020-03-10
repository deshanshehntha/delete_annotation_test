package com.example.demo.service;

import com.example.demo.model.ParticipantAccount;
import com.example.demo.model.ParticipantAccountLocation;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ParticipantAccountMapper implements RowMapper<ParticipantAccount> {
    private static final String PARTICIPANT_ACCOUNT_DB_ID_COLUMN = "participant_account_db_id";
    private static final String PARTICIPANT_ACCOUNT_TABLE_NAME = "participant_account";
    private boolean eagerFetch;
    private Map<Long, ParticipantAccount> participantAccountMap = new HashMap<>();
    private ParticipantAccountLocationMapper locationMapper = new ParticipantAccountLocationMapper();


    public ParticipantAccountMapper() {
        this.eagerFetch = false;
    }

    public ParticipantAccountMapper(boolean eagerFetch) {
        this.eagerFetch = eagerFetch;
    }

    @Override
    public ParticipantAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
        boolean participantAccountContainsInMap = false;
        ParticipantAccount participantAccount = participantAccountMap.get(rs.getLong(PARTICIPANT_ACCOUNT_DB_ID_COLUMN));
        if (participantAccount == null) {
            participantAccount = getParticipantAccount(rs);
        } else {
            participantAccountContainsInMap = true;
        }

        if (participantAccountMap.get(rs.getLong(PARTICIPANT_ACCOUNT_DB_ID_COLUMN)) == null) {
            participantAccountMap.put(participantAccount.getParticipantAccountId(), participantAccount);
            return participantAccount;
        }
        return null;
    }

    private ParticipantAccount getParticipantAccount(ResultSet rs) throws SQLException {

        ParticipantAccount participantAccount = new ParticipantAccount();
        participantAccount.setParticipantAccountId(rs.getObject(PARTICIPANT_ACCOUNT_DB_ID_COLUMN, Long.class));
        participantAccount.setAccountName(rs.getString("account_name"));

        return participantAccount;
    }

    private void setLocation(ParticipantAccount participantAccount, ResultSet rs, int rowNum) throws SQLException {
        if (rs.getObject("location_db_id", Long.class) != null) {
            ParticipantAccountLocation location = locationMapper.mapRow(rs, rowNum);
            if (participantAccount.getLocations() == null) {
                participantAccount.setLocations(new ArrayList<>());
            }
            if (!participantAccount.getLocations().contains(location)) {
                participantAccount.getLocations().add(location);
            }
        }
    }


}
