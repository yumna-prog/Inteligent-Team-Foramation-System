package com.gameclub.team.model;

import com.gameclub.team.service.PersonalityClassifier;

import java.util.List;
import java.util.Objects;

//Represents an individual member
public class Participant {

    private String playerId;
    private String name;
    private String email;

    private String preferredGame;
    private int skillLevel; //-> how does the survey data provides this
    private String preferredRole;

    //Foe each question the rating is taken
    private List<Integer> persona_rating;

    //derived attributes
    private int raw_personalityScore;
    private double normalizedScore;
    private String personalityType;
    private double compositeScore;


    //constants for normalization of personalityScore and skill level
    private static final int max_raw_personalityScore = 25;
    private static final int max_raw_skillLevel = 10;

    public List<Integer> getPersona_rating() {
        return persona_rating;
    }

    //During survey to collect participant data
    public Participant (String playerId, String name, String email){
        this.playerId = playerId;
        this.name = name;
        this.email = email;

    }
    //After survey data Collected
    public Participant(String playerId, String name, String email, String preferredGame, int skillLevel, String preferredRole, List<Integer> persona_rating) {
        this.playerId = playerId;
        this.name = name;
        this.email = email;

        this.preferredGame = preferredGame;
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        this.persona_rating = persona_rating;

        // calculate raw personality_score
        this.raw_personalityScore = calculatePersonalityScore();


        //calculate personality type
        //assign personality using personality Classifier class

        PersonalityClassifier classifier = new PersonalityClassifier();
        this.normalizedScore = (double) this.raw_personalityScore / max_raw_personalityScore *100.0;
        this.personalityType = classifier.classify(this.normalizedScore);
        //calculate composite score and normalize
        this.compositeScore = calculateCompositeScore();


    }
    //For file uploading
    public Participant(String playerId, String name, String email, String preferredGame, int skillLevel, String preferredRole,double normalizedScore,String personalityType){
        this.playerId = playerId;
        this.name = name;
        this.email = email;
        this.preferredGame = preferredGame;
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        this.personalityType = personalityType;
        this.normalizedScore = normalizedScore;
        this.raw_personalityScore = (int) Math.round(normalizedScore/100.0 * 25);
    }

    public Participant(String name, String preferredRole, String personalityType, int skillLevel, String preferredGame) {
        this.name = name;
        this.preferredRole = preferredRole;
        this.personalityType = personalityType;
        this.skillLevel = skillLevel;
        this.preferredGame = preferredGame;
    }



    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPreferredGame() {
        return preferredGame;
    }

    public String getPreferredRole() {
        return preferredRole;
    }

    public int getPersonalityScore() {
        return raw_personalityScore;
    }

    public String getPersonalityType() {
        return personalityType;
    }

    public int getSkillLevel() {
        return Math.max(0,skillLevel);
    }



    public double getNormalizedScore() {
        return this.normalizedScore;
    }

    public double getCompositeScore() {
        return compositeScore;
    }

    public int calculatePersonalityScore() {
        int sum = 0;
        for (int rating : persona_rating) {
            sum += rating;

        }
        return sum;

    }

    public double calculateCompositeScore() {
        double normalizedPersonalityScore = (double) this.raw_personalityScore / max_raw_personalityScore +max_raw_skillLevel *100;
        return this.skillLevel + normalizedPersonalityScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(!(o instanceof Participant)) return false;
        Participant p = (Participant) o;
        return this.playerId.equals(p.playerId);
    }



    @Override
    public int hashCode() {
        return Objects.hash(playerId);
    }


    @Override
    public String toString() {
        return "--- PARTICIPANT ---\n" +
                "Name: " + name + "\n" +
                "Skill Level (1-10): " + skillLevel + "\n" +
                "Game Interest: " + preferredGame + "\n" +
                "Preferred Role: " + preferredRole + "\n" +
                "Personality Type: " + personalityType + " (Mix Constraint Input)\n" +
                "---------------------------------";
    }
     // to display details of each participant in a team
    public String toDisplayString() {
        return String.format("%-15s | %-5s | %-10s | %-10s | %-5d | %s",
                this.name,
                this.playerId,
                this.preferredGame,
                this.preferredRole,
                this.skillLevel,
                this.personalityType);
    }


}

