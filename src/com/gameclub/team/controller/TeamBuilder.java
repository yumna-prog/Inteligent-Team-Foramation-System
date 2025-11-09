package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//Implements the matching algorithm
public class TeamBuilder {

    public List<Team> loadTeams(List<Participant> participants, int teamSize) {
        //1. get the team size, given by user

        //2.calculate the number of participants
        int numberOfParticipants = participants.size();

        //3.calculate the number of teams
        int numberOfTeams = (int) Math.ceil((double) numberOfParticipants/teamSize);

        //4. initialize team list
        List<Team> teams = new ArrayList<>(numberOfTeams);

        //5. instantiate teams
        for (int i = 0; i < numberOfTeams; i++) {
            teams.add(new Team("Team - "+ (i+1),teamSize));
        }
        //Sorting -> the highest skill level to lowest
        //1. use builtin sorting utility
        //2. define comparator -> from each participant object extract score, it  compares the skill level of participant with each other
        //sort it in the descending order
        participants.sort(Comparator.comparing(Participant::getSkillLevel).reversed());



        //core Logic -> calculate the correct teamIndex

        for  (int i = 0; i < numberOfParticipants; i++) {

            //calculate the position of participant in the block
            int positionInBlock =  i% numberOfTeams;

            //determine the current block and direction

            int blockIndex = i / numberOfTeams;
            boolean isReverse = false;
            if (blockIndex %2 != 0){
                isReverse = true;
            }
            int targetTeamIndex;
            if (isReverse) {
                targetTeamIndex = (numberOfTeams - 1) - blockIndex;
            }
            else {
                targetTeamIndex = blockIndex;

            }

            //retrieve the team object
            Team targetTeam = teams.get(targetTeamIndex);
            Participant currentParticipant = participants.get(i);

            // VALIDATION LOGIC



        }


        return teams;
    }

    }




