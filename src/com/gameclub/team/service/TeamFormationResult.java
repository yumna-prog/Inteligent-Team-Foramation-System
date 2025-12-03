package com.gameclub.team.service;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

import java.util.ArrayList;
import java.util.List;

//This act as a container that contains the result of team formation which are teams formed and unassigned participants



public class TeamFormationResult {

    public final List<Team> teams;
    public final List<Participant> unassignedParticipants;


    public List<Team> getTeams() {
        return teams;
    }

    public TeamFormationResult(List<Team> teams, List<Participant> unassignedParticipants) {
        this.teams = teams;
        this.unassignedParticipants = unassignedParticipants;
    }





}
