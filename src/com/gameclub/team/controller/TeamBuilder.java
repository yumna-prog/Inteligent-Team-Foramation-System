package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;
import com.gameclub.team.service.ConstraintChecker;
import com.gameclub.team.service.TeamFormationResult;
import com.gameclub.team.thread.BestSwapInfo;
import com.gameclub.team.thread.SwapEvaluationTask;

import java.sql.ClientInfoStatus;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

//==================================================================================//
//Calculates the required numberOfTeams
//Create Empty Teams -> list
//Grouped participants by personalityType
//Separate all leaders -> why
//sort the non leader players by their composite score -> skill balance by ensuring all teams get high skilled players
//verifies that you have at least one leader for every team
    // if not error and return empty teams
//Sorted leaders are assigned starting from first team , ensure all teams have one leader
//the leaders that are not assigned will be falling to remaining players pool and resorted again
//The snake draft technique ->
    //loops through the remainingPlayers and assign the players
    // While checks if the team is filled and is the game cap exceeded
    // The players that cannot be placed in a team , will be handled by the validation methods

//=====================================================================================//


//Input data-> team size N
//      participant in a arraylist ->

//Algorithm
//1. The composite score = personality score +skill level is calculated for each participant -> done in Participant class
public class TeamBuilder{

    private ConstraintChecker constraintChecker;


    public TeamBuilder() { }

    public void setConstraintChecker(ConstraintChecker constraintChecker) {
        this.constraintChecker = constraintChecker;
    }



    //2. The participants will be sorted based on the composite score
    public List<Participant> sortParticipants(List<Participant> listOfParticipants) {  /* 2.1 - seq*/
        if (listOfParticipants == null || listOfParticipants.isEmpty()) return Collections.emptyList();
        listOfParticipants.sort(Comparator.comparingDouble(Participant::getCompositeScore).reversed());
        return listOfParticipants;  // 2.2 - seq
    }



    public TeamFormationResult formTeams(List<Participant> listOfParticipants, int teamSize, int game_cap) {   /*2.3 - seq */
        List<Team> teams = new ArrayList<>();
        List<Participant> unassignedParticipants = new ArrayList<>();

        if (listOfParticipants == null || listOfParticipants.isEmpty() || teamSize <= 0) return new TeamFormationResult(teams, listOfParticipants); /*2.3.1 - seq */

        int numberOfTeams = (int) Math.ceil((double) listOfParticipants.size() / teamSize);
        for (int j = 0; j < numberOfTeams; j++) {
            teams.add(new Team("Team" + (j + 1)));
        }

        // Hard Constraint: Max 1 Leader Allocation
        List<Participant> allLeaders = listOfParticipants.stream()
                .filter(p -> "Leader".equals(p.getPersonalityType())) /* 2.3.3 - seq*/
                .sorted(Comparator.comparingDouble(Participant::getCompositeScore).reversed()) /* 2.3.4 - seq*/
                .toList();

        List<Participant> nonLeaders = listOfParticipants.stream()
                .filter(p -> !"Leader".equals(p.getPersonalityType())) /* 2.3.5 - seq*/
                .toList();

        // Assign exactly one leader per team (if available)
        List<Participant> assignedLeaders = new ArrayList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            if (i < allLeaders.size()) {
                teams.get(i).addPlayers(allLeaders.get(i));  /*2.3.6 - seq */
                assignedLeaders.add(allLeaders.get(i));
            }
        }

        // --- Prepare remaining players pool ---
        // 1. All Non-Leaders
        List<Participant> remainingPlayers = new ArrayList<>(nonLeaders);
        // 2. All Leaders who were NOT assigned (Excess Leaders)
        List<Participant> excessLeaders = allLeaders.stream()
                .filter(p -> !assignedLeaders.contains(p))
                .toList();

        System.out.println("LOG: Found " + assignedLeaders.size() + " initial leaders assigned. " + excessLeaders.size() + " excess leaders unassigned.");

        remainingPlayers.addAll(excessLeaders);
        remainingPlayers.sort(Comparator.comparingDouble(Participant::getCompositeScore).reversed()); /* 2.3.7 - seq*/

        // Use an index-based loop for easier manipulation of the remainingPlayers list
        for (int pIndex = 0; pIndex < remainingPlayers.size(); ) {
            Participant currentPlayer = remainingPlayers.get(pIndex);
            Team bestFitTeam = null;
            int lowestUniqueRoleCount = Integer.MAX_VALUE; // Start high to find the team needing diversity the most

            // Shuffle teams to introduce fairness if multiple teams have the same lowest role count (P5/Randomization)
            Collections.shuffle(teams);

            for (Team currentTeam : teams) {
                // 1. Hard Check: Team Capacity (P5)
                if (currentTeam.getMembers().size() >= teamSize) {
                    continue;
                }

                // 2. Hard Check: P1/P3 Constraints
                boolean isLeader = "Leader".equals(currentPlayer.getPersonalityType()); /* 2.3.8 - seq*/
                // NOTE: Assuming Participant has getPreferredGame() method
                boolean gameCapMet = currentTeam.getGameCount(currentPlayer.getPreferredGame()) < game_cap; /* 2.3.9 - seq*/
                boolean leaderConstraintMet = !isLeader || currentTeam.getPersonalityCount("Leader") < 1;  /* 2.3.10 - seq*/

                if (gameCapMet && leaderConstraintMet) {

                    // 3. P2 Preference Check: Find team that currently has the fewest unique roles
                    // (This maximizes the chance of adding a new role or balancing role distribution)
                    int currentUniqueRoles = currentTeam.getUniqueRoleCount();   /*2.3.11 - seq */

                    if (currentUniqueRoles < lowestUniqueRoleCount) {
                        lowestUniqueRoleCount = currentUniqueRoles;
                        bestFitTeam = currentTeam;
                    }
                }
            }

            // --- Assignment Decision ---
            if (bestFitTeam != null) {
                // Assign player and remove from the list
                bestFitTeam.addPlayers(currentPlayer);  /* 2.3.12 - seq*/
                remainingPlayers.remove(pIndex); // Use remove(index) to shift subsequent elements
            } else {
                // If no team could accept this player, move to the next player
                // (The player remains in remainingPlayers to be unassigned)
                pIndex++;
            }
        }

        // Any players left in remainingPlayers could not be assigned due to constraints or capacity.
        unassignedParticipants.addAll(remainingPlayers);

        return new TeamFormationResult(teams, unassignedParticipants); /* 2.3.13 - seq*/
    }


    // Team Display Helper (Static utility method)
    public static void displayTeams(TeamFormationResult result) {
        System.out.println("\n*** FINAL TEAM ROSTERS ***");
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

            // Use standard output formatting for the table
            System.out.println(String.format("%-10s | %-10s | %-10s | %-5s | %s",
                    "NAME", "ROLE", "PERSONA", "SKILL", "GAME"));
            System.out.println("-----------|------------|------------|-------|----------------");

            team.getMembers().forEach(p -> System.out.println(String.format("%-10s | %-10s | %-10s | %-5d | %s",
                    p.getName(),
                    p.getPreferredRole(),
                    p.getPersonalityType(),
                    p.getSkillLevel(),
                    p.getPreferredGame())));
        }

        // Print Unassigned Leaders as requested
        List<Participant> unassignedLeaders = result.unassignedParticipants.stream()
                .filter(p -> "Leader".equals(p.getPersonalityType()))
                .toList();

        System.out.println("\n================================================");
        System.out.println("      UNASSIGNED PARTICIPANTS REPORT");
        System.out.println("================================================");

        if (!unassignedLeaders.isEmpty()) {
            System.out.println("The following " + unassignedLeaders.size() + " Leaders could not be assigned (Max 1 Leader per team constraint):");
            unassignedLeaders.forEach(p -> System.out.println("  - " + p));
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



    //Iteratively optimizes the teams using concurrent processing to find the best swap in each round quickly.

    public void optimizeTeamsConcurrent(List<Team> teams) { /*2.4 seq*/

        // --- 1. ExecutorServ2ice Setup ---
        int poolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        System.out.printf("LOG: Initializing Optimization Thread Pool with %d workers.\n", poolSize);

        boolean improvementFound = true;
        int round = 1;
        int maxRounds = 200;

        // Continue looping as long as we find a beneficial swap
        while (improvementFound && round <= maxRounds) {
            improvementFound = false;
            BestSwapInfo bestSwap = null;

            // --- 2. Generate All Potential Swaps (Tasks) ---
            List<Callable<BestSwapInfo>> tasks = new ArrayList<>();

            for (int i = 0; i < teams.size(); i++) {
                for (int j = i + 1; j < teams.size(); j++) {
                    Team teamA = teams.get(i);
                    Team teamB = teams.get(j);

                    if (teamA.getMembers().isEmpty() || teamB.getMembers().isEmpty()) continue;

                    for (Participant playerX : teamA.getMembers()) {
                        for (Participant playerY : teamB.getMembers()) {
                            // Create and store the task
                            tasks.add(new SwapEvaluationTask(teamA, teamB, playerX, playerY, constraintChecker));
                        }
                    }
                }
            }

            if (tasks.isEmpty()) break;

            System.out.printf("--- Optimization Round %d: Evaluating %d total swaps ---\n", round++, tasks.size());

            // --- 3. Parallel Execution and Result Collection ---
            try {
                // invokeAll executes all tasks concurrently and returns Future objects
                List<Future<BestSwapInfo>> futures = executor.invokeAll(tasks);

                double maxImprovement = 0.0;

                // Sequentially analyze the results from the Futures
                for (Future<BestSwapInfo> future : futures) {
                    BestSwapInfo info = future.get();

                    if (info.improvementScore > maxImprovement) {
                        maxImprovement = info.improvementScore;
                        bestSwap = info;
                        improvementFound = true;
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("ERROR: Interruption or execution issue during concurrent swap: " + e.getMessage());
                Thread.currentThread().interrupt();
            }

            // --- 4. Apply the Best Change Sequentially (State Update) ---
            if (improvementFound && bestSwap != null && bestSwap.improvementScore > 0.0) {

                Team teamA = findTeamByName(teams, bestSwap.teamAName); /*2.4.3*/
                Team teamB = findTeamByName(teams, bestSwap.teamBName);

                Participant playerX = teamA.getMemberByName(bestSwap.participantXName);
                Participant playerY = teamB.getMemberByName(bestSwap.participantYName);

                if (teamA != null && teamB != null && playerX != null && playerY != null) {
                    // Critical: This modification MUST be sequential in the main thread.
                    teamA.getMembers().remove(playerX);
                    teamB.getMembers().remove(playerY);
                    teamA.getMembers().add(playerY);
                    teamB.getMembers().add(playerX);

                    System.out.printf("LOG: Applied best swap (Improvement: %.2f) between %s (%s) and %s (%s).\n",
                            bestSwap.improvementScore, playerX.getName(), teamA.getTeamName(), playerY.getName(), teamB.getTeamName());
                } else {
                    System.err.println("WARNING: Could not apply best swap due to missing object references. Stopping optimization.");
                    improvementFound = false;
                }
            } else {
                improvementFound = false;
            }
        }

        // --- 5. Cleanup ---
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










































