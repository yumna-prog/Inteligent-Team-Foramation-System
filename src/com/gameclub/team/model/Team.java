package com.gameclub.team.model;

//Represents a collection of members
public class Team {


    private int teamSize;
    private String teamId;

    public Team(String teamId, int teamSize) {
        this.teamSize = teamSize;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

}
