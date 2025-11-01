package com.gameclub.team.model;

public class Participant {

    private int playerId;
    private String preferredGame;
    private int skillLevel;
    private String preferredRole;
    private int personalityScore;
    private String personalityType;

    public Participant() {
    }

    public Participant(int playerId, String preferredGame,int skillLevel, String preferredRole, int personalityScore, String personalityType) {
        this.playerId = playerId;
        this.preferredGame = preferredGame;
        this.preferredRole = preferredRole;
        this.personalityScore = personalityScore;
        this.personalityType = personalityType;
        this.skillLevel = skillLevel;
    }

    public int getPlayerId() {
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


    @Override
    public String toString() {
        return"Participant [playerId=" + playerId + ", preferredGame=" + preferredGame + ", preferredRole=" + preferredRole + ", personalityScore=" + personalityScore;
    }
}
