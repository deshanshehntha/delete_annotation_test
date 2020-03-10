package com.example.demo.model;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantAccount {

    private Long participantAccountId;
    private String accountName;
    private List<ParticipantAccountLocation> locations;

}
