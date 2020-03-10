package com.example.demo.service;


import com.example.demo.dao.LocationDAO;
import com.example.demo.dao.ParticipantAccountDAO;
import com.example.demo.model.Location;
import com.example.demo.model.ParticipantAccount;
import com.example.demo.model.ParticipantAccountLocation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ParticipantAccountService {

    @Autowired
    ParticipantAccountDAO participantAccountDAO;

    @Autowired
    LocationDAO locationDAO;

//    public void updateParticipantAccount(Long participantAccountId,
//                                         ParticipantAccount participantAccountFromRequest) {
//
//
//        ParticipantAccount participantAccountFromPersistence = new ParticipantAccount();
//        participantAccountFromPersistence.setParticipantAccountId(10000L);
//        participantAccountFromPersistence.setAccountName("Account1");
//        List<ParticipantAccountLocation> participantAccountLocationList = new ArrayList<>();
//        ParticipantAccountLocation participantAccountLocation = new ParticipantAccountLocation();
//        participantAccountLocation.setLocationId(1L);
//        participantAccountLocation.setLocationName("Kandy");
//        participantAccountLocationList.add(participantAccountLocation);
//        ParticipantAccountLocation participantAccountLocation1 = new ParticipantAccountLocation();
//        participantAccountLocation1.setLocationId(2L);
//        participantAccountLocation1.setLocationName("Colombo");
//        participantAccountLocationList.add(participantAccountLocation1);
//        participantAccountFromPersistence.setLocations(participantAccountLocationList);
//
//
//        ParticipantAccount modifiedParticipantAccount = getParticipantAccWithDefaultValuesForUpdate(
//                participantAccountFromRequest,
//                participantAccountFromPersistence);
//
//
//        updateParticipantAccountRelatedDetails(participantAccountId,
//                participantAccountFromPersistence,
//                modifiedParticipantAccount);
//
//    }


    public void updateParticipantAccount(Long participantAccountId, ParticipantAccount participantAccountFromRequest) {

        ParticipantAccount participantAccountFromPersistence = participantAccountDAO.getParticipantAccountById(participantAccountId);

        List<ParticipantAccountLocation> participantAccountLocationList = new ArrayList<>();


        ParticipantAccountLocation participantAccountLocation1 = new ParticipantAccountLocation();
        participantAccountLocation1.setLocationId(1L);
        participantAccountLocation1.setLocationName("Sinhala");

        participantAccountLocationList.add(participantAccountLocation1);

        ParticipantAccountLocation participantAccountLocation = new ParticipantAccountLocation();
        participantAccountLocation.setLocationId(2L);
        participantAccountLocation.setLocationName("Sinhala");

        participantAccountLocationList.add(participantAccountLocation);


        ParticipantAccountLocation participantAccountLocation2 = new ParticipantAccountLocation();
        participantAccountLocation2.setLocationId(3L);
        participantAccountLocation2.setLocationName("Sinhala");

        participantAccountLocationList.add(participantAccountLocation2);

        participantAccountFromPersistence.setLocations(participantAccountLocationList);

        //finished creating object from db
        // start updating

        ParticipantAccount modifiedParticipantAccount = getParticipantAccWithDefaultValuesForUpdate(participantAccountFromRequest, participantAccountFromPersistence);

        updateParticipantAccountRelatedDetails(participantAccountId, participantAccountFromPersistence, modifiedParticipantAccount);

    }


    private void updateParticipantAccountRelatedDetails(Long participantAccountId, ParticipantAccount persistedParticipantAccount, ParticipantAccount modifiedParticipantAccount) {

        boolean success = participantAccountDAO.updateParticipantAccount(participantAccountId, modifiedParticipantAccount);

        success = updateParticipantAccountLocations(participantAccountId, persistedParticipantAccount, modifiedParticipantAccount);

    }


    private boolean updateParticipantAccountLocations(Long participantAccountId,
                                                      ParticipantAccount participantAccountFromPersistence,
                                                      ParticipantAccount modifiedParticipantAccount) {
        boolean success = true;

        if (true) {

            List<Long> locationIdsFromPersistence = participantAccountFromPersistence.getLocations()
                    .stream()
                    .map(ParticipantAccountLocation::getLocationId)
                    .collect(Collectors.toList());

            List<Long> locationIdsOfModifiedAccLocations = modifiedParticipantAccount.getLocations()
                    .stream()
                    .map(ParticipantAccountLocation::getLocationId)
                    .collect(Collectors.toList());

            HashMap<String, List<Long>> idsToBeDeletedAndUpdated = getLocationIdsToBeDeletedAndUpdated(locationIdsFromPersistence, locationIdsOfModifiedAccLocations);

            success = locationDAO.deleteLocations(idsToBeDeletedAndUpdated.get("IDS_TO_BE_DELETED")); /// its working

            if (true) {
                success = participantAccountDAO.deleteParticipantAccLocations(participantAccountId, idsToBeDeletedAndUpdated.get("IDS_TO_BE_DELETED"));

                if (true) {
                    createParticipantAccountLocations(participantAccountId,
                            idsToBeDeletedAndUpdated.get("IDS_TO_BE_UPDATED"),
                            idsToBeDeletedAndUpdated.get("IDS_TO_BE_INSERTED"),
                            modifiedParticipantAccount.getLocations());
                }
            }
        }
        return success;
    }

    private HashMap<String, List<Long>> getLocationIdsToBeDeletedAndUpdated(List<Long> locationIdsFromPersistence, List<Long> locationIdsOfModifiedAccLocations) {

        List<Long> idsToBeDeleted = new ArrayList<>();
        List<Long> idsToBeUpdated = new ArrayList<>();
        List<Long> idsToBeInsterted = new ArrayList<>();

        for (Long id : locationIdsFromPersistence) {
            if (!locationIdsOfModifiedAccLocations.contains(id)) {
                idsToBeDeleted.add(id);
            } else {
                idsToBeUpdated.add(id);
            }
        }

        for (Long id : locationIdsOfModifiedAccLocations) {
            if (!idsToBeUpdated.contains(id)) {
                idsToBeInsterted.add(id);
            }
        }


        HashMap<String, List<Long>> idsMap = new HashMap<>();
        idsMap.put("IDS_TO_BE_DELETED", idsToBeDeleted);
        idsMap.put("IDS_TO_BE_UPDATED", idsToBeUpdated);
        idsMap.put("IDS_TO_BE_INSERTED", idsToBeInsterted);
        return idsMap;
    }


    private ParticipantAccount getParticipantAccWithDefaultValuesForUpdate(ParticipantAccount participantAccInRequest,
                                                                           ParticipantAccount participantAccountFromPersistence
    ) {
        ParticipantAccount modifiedParticipantAccount = new ParticipantAccount();
        modifiedParticipantAccount.setParticipantAccountId(participantAccInRequest.getParticipantAccountId());
        modifiedParticipantAccount.setAccountName(participantAccInRequest.getAccountName());


        if (!CollectionUtils.isEmpty(participantAccInRequest.getLocations())) {
            modifiedParticipantAccount.setLocations(getLocationsWithDefaultValues(participantAccInRequest.getLocations()));
        }

        return modifiedParticipantAccount;
    }

    private List<ParticipantAccountLocation> getLocationsWithDefaultValues(
            List<ParticipantAccountLocation> receivedLocations) {

        List<ParticipantAccountLocation> newLocations = new ArrayList<>(receivedLocations.size());
        ParticipantAccountLocation primaryLocation = null;

        for (ParticipantAccountLocation receivedLocation : receivedLocations) {
            ParticipantAccountLocation newLocation = new ParticipantAccountLocation();
            BeanUtils.copyProperties(receivedLocation, newLocation);


            newLocations.add(newLocation);
        }

        return newLocations;
    }


    private void createParticipantAccountLocations(long participantAccountId, List<Long> idsToBeUpdated,
                                                   List<Long> idsToBeInsterted,
                                                   List<ParticipantAccountLocation> participantAccountLocations) {

        if (!CollectionUtils.isEmpty(participantAccountLocations)) {

            List<Location> locationsToInserted = getLocationsToInsert(participantAccountLocations, idsToBeInsterted);
            List<ParticipantAccountLocation> participantAccountLocationsToInsert = getParticipantAccountLocations(participantAccountLocations, idsToBeInsterted);
            List<ParticipantAccountLocation> participantAccountLocationsToUpdate = getParticipantAccountLocations(participantAccountLocations, idsToBeUpdated);


            List<Long> locationIds = locationDAO.createLocations(locationsToInserted);

            if (CollectionUtils.isEmpty(locationIds) || locationIds.size() != locationsToInserted.size()) {
                //Throw Error
            }


            for (int i = 0; i < locationsToInserted.size(); i++) {
                ParticipantAccountLocation location = participantAccountLocationsToInsert.get(i);
                if (location != null) {
                    location.setLocationId(locationIds.get(i));
                }
            }

            boolean locationMappingsCreated = participantAccountDAO.createParticipantAccountLocations(participantAccountId, participantAccountLocationsToInsert);

            participantAccountDAO.updateParticipantAccountLocations(participantAccountId, participantAccountLocationsToUpdate);

        }
    }

    private List<Location> getLocationsToInsert(List<ParticipantAccountLocation> participantAccountLocations, List<Long> idsToBeInsterted) {

        return participantAccountLocations.stream()
                .map(participantAccountLocation -> {
                    for (Long id : idsToBeInsterted) {
                        if (id.equals(participantAccountLocation.getLocationId())) {
                            Location location = new Location();
                            BeanUtils.copyProperties(participantAccountLocation, location);
                            return location;
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private List<ParticipantAccountLocation> getParticipantAccountLocations(List<ParticipantAccountLocation> participantAccountLocations,
                                                                            List<Long> idsToBeInsterted) {
        return participantAccountLocations.stream()
                .map(participantAccountLocation -> {
                    for (Long id : idsToBeInsterted) {
                        if (id.equals(participantAccountLocation.getLocationId())) {
                            return participantAccountLocation;
                        }
                    }
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
