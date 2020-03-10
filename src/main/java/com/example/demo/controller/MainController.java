package com.example.demo.controller;


import com.example.demo.model.ParticipantAccount;
import com.example.demo.model.ParticipantAccountLocation;
import com.example.demo.service.ParticipantAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MainController {

    @Autowired
    private ParticipantAccountService participantAccountService;


    @RequestMapping("api/updateParticipantAccount")
    public void saveProduct() {

        ParticipantAccount participantAccount = new ParticipantAccount();
        participantAccount.setParticipantAccountId(10000L);
        participantAccount.setAccountName("Account5");

        List<ParticipantAccountLocation> participantAccountLocationList = new ArrayList<>();

        ParticipantAccountLocation participantAccountLocation1 = new ParticipantAccountLocation();
        participantAccountLocation1.setLocationId(1L);
        participantAccountLocation1.setLocationName("My Location1");

        participantAccountLocationList.add(participantAccountLocation1);


        ParticipantAccountLocation participantAccountLocation = new ParticipantAccountLocation();
        participantAccountLocation.setLocationId(2L);
        participantAccountLocation.setLocationName("My Location");

        participantAccountLocationList.add(participantAccountLocation);

        ParticipantAccountLocation participantAccountLocation3 = new ParticipantAccountLocation();
        participantAccountLocation3.setLocationId(4L);
        participantAccountLocation3.setLocationName("My Location");

        participantAccountLocationList.add(participantAccountLocation3);

        ParticipantAccountLocation participantAccountLocation4 = new ParticipantAccountLocation();
        participantAccountLocation4.setLocationId(5L);
        participantAccountLocation4.setLocationName("My Location");

        participantAccountLocationList.add(participantAccountLocation4);


        participantAccount.setLocations(participantAccountLocationList);


        participantAccountService.updateParticipantAccount(participantAccount.getParticipantAccountId(), participantAccount);
    }

}