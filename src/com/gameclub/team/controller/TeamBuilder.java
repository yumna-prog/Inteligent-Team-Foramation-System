package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;
import com.gameclub.team.service.ConstraintChecker;
import com.gameclub.team.service.TeamFormationResult;
import com.gameclub.team.thread.BestSwapInfo;
import com.gameclub.team.thread.SwapEvaluationTask;

import java.util.*;
import java.util.concurrent.*;


//==================================================================================//
//Calculates the required numberOfTeams
//Create Empty Teams -> list
//Grouped participants by personalityType
//Separate all leaders
//Sort the non leader players by their composite score -> skill balance by ensuring all teams get high skilled players
//Verifies that you have at least one leader for every team
//Sorted leaders are assigned starting from first team , ensure all teams have one leader
//the leaders that are not assigned will be falling to remaining players pool and resorted again
//The snake draft technique ->
    //loops through the remainingPlayers and assign the players
    // While checks if the team is filled and is the game cap exceeded
    // The players that cannot be placed in a team , will be handled by the validation methods
//=====================================================================================//


//Algorithm
//The composite score = personality score +skill level is calculated for each participant -> done in Participant class
public class TeamBuilder{

    private ConstraintChecker constraintChecker;

    public TeamBuilder() { }

    public void setConstraintChecker(ConstraintChecker constraintChecker) {
        this.constraintChecker = constraintChecker;
    }

    //The participants will be sorted based on the composite score
    public List<Participant> sortParticipants(List<Participant> listOfParticipants) {  /* 2.1 - seq*/
        if (listOfParticipants == null || listOfParticipants.isEmpty()) return Collections.emptyList();
        listOfParticipants.sort(Comparator.comparingDouble(Participant::getCompositeScore).reversed());
        return listOfParticipants;  // 2.2 - seq
    }



    public TeamFormationResult formTeams(List<Participant> listOfParticipants, int teamSize, int game_cap) {   /*2.3 - seq */
        List<Team> teams = new ArrayList<>();
        List<Participant> unassignedParticipants = new ArrayList<>();

        try {
            if (listOfParticipants == null || listOfParticipants.isEmpty() || teamSize <= 0)
                return new TeamFormationResult(teams, listOfParticipants); /*2.3.1 - seq */

            int numberOfTeams = (int) Math.ceil((double) listOfParticipants.size() / teamSize);
            for (int j = 0; j < numberOfTeams; j++) {
                teams.add(new Team("Team" + (j + 1)));
            }

            // Constraint: Max 1 Leader Allocation
            List<Participant> allLeaders = listOfParticipants.stream()
                    .filter(p -> "Leader".equals(p.getPersonalityType())) /* 2.3.3 - seq*/
                    .sorted(Comparator.comparingDouble(Participant::getCompositeScore).reversed()) /* 2.3.4 - seq*/
                    .toList();

            List<Participant> nonLeaders = listOfParticipants.stream()
                    .filter(p -> !"Leader".equals(p.getPersonalityType())) /* 2.3.5 - seq*/
                    .toList();

            // Assign exactly one leader per team
            List<Participant> assignedLeaders = new ArrayList<>();
            for (int i = 0; i < numberOfTeams; i++) {
                if (i < allLeaders.size()) {
                    teams.get(i).addPlayers(allLeaders.get(i));  /*2.3.6 - seq */
                    assignedLeaders.add(allLeaders.get(i));
                }
            }
            //Prepare remaining players pool
            // 1. All Non-Leaders
            List<Participant> remainingPlayers = new ArrayList<>(nonLeaders);
            // 2. All Leaders who were NOT assigned
            List<Participant> excessLeaders = allLeaders.stream()
                    .filter(p -> !assignedLeaders.contains(p))
                    .toList();

            remainingPlayers.addAll(excessLeaders);
            remainingPlayers.sort(Comparator.comparingDouble(Participant::getCompositeScore).reversed()); /* 2.3.7 - seq*/

            for (int pIndex = 0; pIndex < remainingPlayers.size(); ) {
                try {
                    Participant currentPlayer = remainingPlayers.get(pIndex);
                    Team bestFitTeam = null;
                    int lowestUniqueRoleCount = Integer.MAX_VALUE; // Start high to find the team needing diversity the most

                    // Shuffle teams to introduce fairness if multiple teams have the same lowest role count (P5/Randomization)
                    Collections.shuffle(teams);

                    for (Team currentTeam : teams) {
                        if (currentTeam.getMembers().size() >= teamSize) {
                            continue;
                        }

                        boolean isLeader = "Leader".equals(currentPlayer.getPersonalityType()); /* 2.3.8 - seq*/
                        boolean gameCapMet = currentTeam.getGameCount(currentPlayer.getPreferredGame()) < game_cap; /* 2.3.9 - seq*/
                        boolean leaderConstraintMet = !isLeader || currentTeam.getPersonalityCount("Leader") < 1;  /* 2.3.10 - seq*/

                        if (gameCapMet && leaderConstraintMet) {

                            // Preference Check: Find team that currently has the fewest unique roles
                            // (This maximizes the chance of adding a new role or balancing role distribution)
                            int currentUniqueRoles = currentTeam.getUniqueRoleCount();   /*2.3.11 - seq */

                            if (currentUniqueRoles < lowestUniqueRoleCount) {
                                lowestUniqueRoleCount = currentUniqueRoles;
                                bestFitTeam = currentTeam;
                            }
                        }
                    }

                    if (bestFitTeam != null) {
                        // Assign player and remove from the list
                        bestFitTeam.addPlayers(currentPlayer);  /* 2.3.12 - seq*/
                        remainingPlayers.remove(pIndex);
                    }else{
                        pIndex++;
                    }
                }catch (Exception e) {
                    System.err.println("Failed to assign leaders. Cause :" + e.getMessage());
                    pIndex++;
                }

            }
            // Any players left in remainingPlayers could not be assigned due to constraints or capacity
            unassignedParticipants.addAll(remainingPlayers);

        } catch (Exception e) {
            System.err.println("Error: Unexpected issue in formTeams. Cause :" + e.getMessage());
        }

        return new TeamFormationResult(teams, unassignedParticipants); /* 2.3.13 - seq*/
    }


    // Team Display
    public static void displayTeams(TeamFormationResult result) {
        System.out.println("\n*** FINAL TEAMS ***");
        if (result.teams.isEmpty()) {
            System.out.println("No teams were successfully formed.");
            return;
        }

        for (Team team : result.teams) {
            double avgSkill;
            if(team.getMembers().isEmpty()){
                avgSkill = 0;
            }else{
                 avgSkill = team.getTotalSkill() / (double)team.getMembers().size();
            }
            System.out.println("\n--- " + team.getTeamName() + " (Members: " + team.getMembers().size() + ", Avg Skill: " + String.format("%.2f", avgSkill) + ") ---");

            System.out.printf("%-10s | %-10s | %-10s | %-5s | %s%n",
                    "NAME", "ROLE", "PERSONA", "SKILL", "GAME");
            System.out.println("-----------|------------|------------|-------|----------------");

            team.getMembers().forEach(p -> System.out.printf("%-10s | %-10s | %-10s | %-5d | %s%n",
                    p.getName(),
                    p.getPreferredRole(),
                    p.getPersonalityType(),
                    p.getSkillLevel(),
                    p.getPreferredGame()));
        }

        // Print Unassigned Leaders
        List<Participant> unassignedLeaders = result.unassignedParticipants.stream()
                .filter(p -> "Leader".equals(p.getPersonalityType()))
                .toList();

        System.out.println("\n================================================");
        System.out.println("      UNASSIGNED PARTICIPANTS REPORT");
        System.out.println("================================================");

        if (!unassignedLeaders.isEmpty()) {
            System.out.println("The following Leaders could not be assigned (Max 1 Leader per team constraint):");
            unassignedLeaders.forEach(p ->
                    System.out.printf("  - %s (Role: %s, Game: %s, Skill: %d)%n",
                            p.getName(),
                            p.getPreferredRole(),
                            p.getPreferredGame(),
                            p.getSkillLevel())
            );
        } else {
            System.out.println("All available Leaders were successfully assigned, one per team.");
        }

        if (result.unassignedParticipants.size() > unassignedLeaders.size()) {
            System.out.println("\nAdditionally, " + (result.unassignedParticipants.size() - unassignedLeaders.size()) + " other players remain unassigned (due to game cap or team capacity):");
            result.unassignedParticipants.stream()
                    .filter(p -> !"Leader".equals(p.getPersonalityType()))
                    .forEach(p -> System.out.println("  - " + p));
        }
        System.out.println("================================================");
    }


    public static void displayOnlyTeams(TeamFormationResult result) {
        if (result == null || result.getTeams().isEmpty()) {
            System.out.println(" No teams were formed yet.");
            return;
        }

        System.out.println("\n*** CURRENT TEAMS ***");
        for (Team team : result.getTeams()) {
            System.out.println("\n" + team.getTeamName() + ":");
            if (team.getMembers().isEmpty()) {
                System.out.println("  (No members assigned yet)");
            } else {
                for (Participant p : team.getMembers()) {
                    System.out.println("  - " + p.getName() + " (" + p.getPersonalityType() + ")");
                }
            }
        }
    }


    //Iteratively optimizes the teams using concurrent processing to find the best swap in each round quickly.
    public void optimizeTeamsConcurrent(List<Team> teams) { /*2.4 seq*/

        //ExecutorServ2ice Setup
        int poolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);


        System.out.printf("LOG: Initializing Optimization Thread Pool with %d workers.\n", poolSize);

        boolean improvementFound = true;
        int round = 1;
        int maxRounds = 100;

        // Continue looping as long as we find a beneficial swap
        while (improvementFound && round <= maxRounds) {
            improvementFound = false;
            BestSwapInfo bestSwap = null;

            //Generate All Potential Swaps
            List<Callable<BestSwapInfo>> tasks = new ArrayList<>();

            for (int i = 0; i < teams.size(); i++) {
                for (int j = i + 1; j < teams.size(); j++) {
                    Team teamA = teams.get(i);
                    Team teamB = teams.get(j);

                    if (teamA.getMembers().isEmpty() || teamB.getMembers().isEmpty()) continue;

                    for (Participant playerX : teamA.getMembers()) {
                        for (Participant playerY : teamB.getMembers()) {
                            tasks.add(new SwapEvaluationTask(teamA, teamB, playerX, playerY, constraintChecker));
                        }
                    }
                }
            }

            if (tasks.isEmpty()) break;

            try {
                List<Future<BestSwapInfo>> futures = executor.invokeAll(tasks);

                double maxImprovement = 0.0;

                for (Future<BestSwapInfo> future : futures) {
                    try {
                        BestSwapInfo info = future.get();

                        if (info.improvementScore > maxImprovement) {
                            maxImprovement = info.improvementScore;
                            bestSwap = info;
                            improvementFound = true;
                        }
                    } catch (ExecutionException e) {
                        System.out.println("Swap Evaluation failed");


                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            if (improvementFound && bestSwap.improvementScore > 0.0) {

                Team teamA = findTeamByName(teams, bestSwap.teamAName); /*2.4.3*/
                Team teamB = findTeamByName(teams, bestSwap.teamBName);

                Participant playerX = teamA.getMemberByName(bestSwap.participantXName);
                Participant playerY = teamB.getMemberByName(bestSwap.participantYName);

                try {
                    if (teamA != null && teamB != null && playerX != null && playerY != null) {
                        teamA.getMembers().remove(playerX);
                        teamB.getMembers().remove(playerY);
                        teamA.getMembers().add(playerY);
                        teamB.getMembers().add(playerX);


                    } else {
                        improvementFound = false;
                    }
                } catch (NullPointerException e) {
                    System.err.println("Error:" +e.getMessage());
                    improvementFound = false;
                }
            } else {
                improvementFound = false;
            }
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("LOG: Concurrent Optimization finished. Thread pool shut down.");
    }

    // Helper method to safely retrieve the team object by name
    private Team findTeamByName(List<Team> teams, String name) {
        for (Team team : teams) {
            if (team.getTeamName().equals(name)) {
                return team;
            }
        }
        return null;
    }



}










































