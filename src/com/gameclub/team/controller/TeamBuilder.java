package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//Input data-> team size N
//      participant in a arraylist ->

//Algorithm
//1. The composite score = personality score +skill level is calculated for each participant
//2. The participants will be sorted based on the composite score
//3. The sorted participants  will be distributed using the snake-draft
    //a. initializes the required number of empty teams based on number of participants and team size
    //b. Forward distribution round
        // assign the players for each team from the highest rank accordingly
    //c. Turning point
        //Once a player has been assigned to the very last team, the algo revers the direction
    //d. Backwards distribution round
        //assign the players for each team moving backwards, start from the team that last assigned
    // Repeat until all players are assigned

//4. Check for the constraints
// The algorithm checks a constraint, and if it fails, it fixes the failure, and then checks all constraints again until every rule is met.
    //a. Check the cap for each game -
        //initialize the cap per game
        // iterate  through each team and count how many players prefer each game type
        // algorithm compares the count for the most common game against the defined cap
    //IF FAILS ->

    //b. Check for role diversity -
        //initialize the minimum unique roles per team
        // iterate  through each team and count how many unique roles
        // compares the count against the minimum requirement
    //IF FAILS ->

    //c. Check for skill balance  - ensure the above changes  haven't made the overall skill imbalanced
    // every final team, calculate the average skill score
    //  Calculate the algorithm variance between the avg skill scores
    // validate using a threshold (1 point) , the algorithm will repeat adjustment

    //d. If the system finds two different ways to arrange the players that both fully satisfy every single constraint
        // collects all the equally valid team combinations
        // uses a random number generator to pick one













































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




