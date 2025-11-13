package com.gameclub.team.model;

import java.util.ArrayList;
import java.util.List;

//Represents a collection of members
public class Team {


    private int teamSize;
    private String teamId;
    //participant list
    private List<Participant> members;

    public Team() {
        members = new ArrayList<>(); // what creating a team object a list will be created to add the members
         }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }
    //add participants
    public void addPlayers(Participant p) {
        members.add(p);

    }
    //get the participants
    public List<Participant> getMembers() {
        return members;
    }


}
