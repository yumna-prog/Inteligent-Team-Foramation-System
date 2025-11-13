package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

public class PersonalityClassifier{

    //Check for Leader -> 90-100
            // Balanced -> 70-89
            // Thinker -> 50-69
    // else -> ????????
    public String classify(int score) {
        if (score >= 90 && score <= 100) {
            return "Leader";
        }
        else if (score >= 70 && score <= 89) {
            return "Balanced";
        }
        else if (score >= 50 && score <= 69) {
            return "Thinker";
        }
        else {
            return "null";

        }
    }
    //dummy composite score calculation for core logic
    public int calculateCompositeScore(Participant p) {
        return p.getSkillLevel() + p.getPersonalityScore();
    }
}
