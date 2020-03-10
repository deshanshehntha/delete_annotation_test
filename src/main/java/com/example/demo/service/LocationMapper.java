package com.example.demo.service;

import com.example.demo.model.Location;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;


public class LocationMapper implements RowMapper<Location> {
    private static final String LOCATION_TABLE_NAME = "location";

    @Override
    public Location mapRow(ResultSet rs, int rowNum) throws SQLException {

        Location location = new Location();
        location.setLocationId(rs.getLong("location_db_id"));
        location.setLocationName(rs.getString("location_name"));


        return location;
    }
}
