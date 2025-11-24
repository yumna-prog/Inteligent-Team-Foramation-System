package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

public class PersonalityClassifier{

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
