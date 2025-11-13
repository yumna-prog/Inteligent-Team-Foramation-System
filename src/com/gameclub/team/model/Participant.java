package com.gameclub.team.model;

//Represents an individual member
public class Participant {

    private String playerId;
    private String name;
    private String emailMail;
    private String preferredGame;
    private int skillLevel; //-> how does the survey data provides this
    private String preferredRole;
    private int personalityScore;
    private String personalityType;      //PersonalityClassifier Datatype

    private int compositeScore;


    public Participant(String name, String preferredGame, int skillLevel, String preferredRole,String personalityType, int compositeScore) {

        this.name = name;
        this.preferredGame = preferredGame;
        this.skillLevel = skillLevel;
        this.preferredRole = preferredRole;
        //this.personalityScore = personalityScore;
        this.personalityType = personalityType;
        this.compositeScore = compositeScore;

    }

    public Participant() {
    }

//    public Participant(String playerId, String preferredGame,int skillLevel, String preferredRole, int personalityScore, String personalityType) {
//        this.playerId = playerId;
//        this.preferredGame = preferredGame;
//        this.preferredRole = preferredRole;
//        this.personalityScore = personalityScore;
//        this.personalityType = personalityType;
//        this.skillLevel = skillLevel;
//    }
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailMail() {
        return emailMail;
    }

    public void setEmailMail(String emailMail) {
        this.emailMail = emailMail;
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
        return personalityScore;
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
        this.personalityScore = personalityScore;
    }

    public void setPersonalityType(String personalityType) {
        this.personalityType = personalityType;
    }

    public void setSkillLevel(int SkillLevel) {
        skillLevel = SkillLevel;
    }

    public int getCompositeScore() {
        return getSkillLevel() + getPersonalityScore();
    }


    @Override
    public String toString() {
        return"Participant [playerId=" + playerId + ", preferredGame=" + preferredGame + ", preferredRole=" + preferredRole + ", personalityScore=" + personalityScore;
    }
}
