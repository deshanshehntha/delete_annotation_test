package com.example.demo.service;

import com.example.demo.model.Location;
import com.example.demo.model.ParticipantAccountLocation;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class ParticipantAccountLocationMapper implements RowMapper<ParticipantAccountLocation> {
    private static final String PARTICIPANT_ACCOUNT_LOCATION_TABLE_NAME = "participant_account_location";

    @Override
    public ParticipantAccountLocation mapRow(ResultSet rs, int rowNum) throws SQLException {

        Location location = (new LocationMapper()).mapRow(rs, rowNum);
        ParticipantAccountLocation participantAccountLocation = new ParticipantAccountLocation();
        BeanUtils.copyProperties(location, participantAccountLocation);
//        participantAccountLocation.setParticipantAccountId(getColumnValueByTableName(rs, PARTICIPANT_ACCOUNT_LOCATION_TABLE_NAME,
//            "fk_participant_account_id", Long.class));
//        if (rs.getObject("fk_location_type", Long.class) != null) {
//            LocationType locationType = LocationType.getLocationById(rs.getObject("fk_location_type", Long.class));
//            participantAccountLocation.setLocationType(locationType);
//        }

        return participantAccountLocation;
    }
}
