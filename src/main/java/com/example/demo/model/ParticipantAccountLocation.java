package com.example.demo.model;

import lombok.Data;

import java.util.Comparator;

@Data
public class ParticipantAccountLocation extends Location {

    /*Comparator for sorting the list by roll no*/
    public static Comparator<ParticipantAccountLocation> idComparator = new Comparator<ParticipantAccountLocation>() {

        @Override
        public int compare(ParticipantAccountLocation participantAccountLocation1, ParticipantAccountLocation participantAccountLocation2) {


            int id2 = participantAccountLocation2.getLocationId().intValue();
            int id1 = participantAccountLocation1.getLocationId().intValue();

            /*For ascending order*/
            return id1 - id2;

            /*For descending order*/
            //rollno2-rollno1;
        }
    };

}
