package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

public class PersonalityClassifier{

    //Check for Leader -> 90-100
            // Balanced -> 70-89
            // Thinker -> 50-69
    // else -> ????????
    public String classify(double normalizedScore) {
        if (normalizedScore >= 90) {
            return "Leader";
        }
        else if (normalizedScore >= 70) {
            return "Balanced";
        }
        else if (normalizedScore >= 50) {
            return "Thinker";
        }
        else{
            throw new IllegalArgumentException("Invalid  personality score: " + normalizedScore);

        }

    }


}
