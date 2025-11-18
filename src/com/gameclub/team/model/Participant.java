package com.gameclub.team.model;

import com.gameclub.team.service.PersonalityClassifier;

import java.util.List;
import java.util.Objects;

//Represents an individual member
public class Participant {

    private final String playerId;
    private String name;
    private String email;

    private String preferredGame;
    private int skillLevel; //-> how does the survey data provides this
    private String preferredRole;

    //Foe each question the rating is taken
    private List<Integer> persona_rating;

    //derived attributes
    private int raw_personalityScore;
    private String personalityType;
    private double compositeScore;


    //constants for normalization of personalityScore and skill level
    private static final int max_raw_personalityScore = 25;
    private static final int max_raw_skillLevel = 10;

    public List<Integer> getPersona_rating() {
        return persona_rating;
    }

    public int getRaw_personalityScore() {
        return raw_personalityScore;
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
        double normalizedScore = (double) this.raw_personalityScore / max_raw_personalityScore *100.0;
        this.personalityType = classifier.classify(normalizedScore);

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
        this.raw_personalityScore = (int) Math.round(normalizedScore/100.0 * 25);
    }


    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmailMail(String emailMail) {
        this.email = emailMail;
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
        return skillLevel;
    }

    public void setPreferredGame(String preferredGame) {
        this.preferredGame = preferredGame;
    }

    public void setPreferredRole(String preferredRole) {
        this.preferredRole = preferredRole;
    }

    public void setPersonalityScore(int personalityScore) {
        this.raw_personalityScore = personalityScore;
    }

    public void setPersonalityType(String personalityType) {
        this.personalityType = personalityType;
    }

    public void setSkillLevel(int SkillLevel) {
        skillLevel = SkillLevel;
    }


    public void setCompositeScore(int compositeScore) {
        this.compositeScore = compositeScore;
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
    }

