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

    // Dependency Injection Constructor (Preferred for testing)
    public TeamBuilder(ConstraintChecker constraintChecker) {
        this.constraintChecker = constraintChecker;
    }

    public TeamBuilder() {

    }

    public void setConstraintChecker(ConstraintChecker constraintChecker) {
        this.constraintChecker = constraintChecker;
    }



    //2. The participants will be sorted based on the composite score
    public List<Participant> sortParticipants(List<Participant> listOfParticipants) {
        if (listOfParticipants == null || listOfParticipants.isEmpty()) return Collections.emptyList();
        listOfParticipants.sort(Comparator.comparingDouble(Participant::getCompositeScore).reversed());
        return listOfParticipants;
    }



    public TeamFormationResult formTeams(List<Participant> listOfParticipants, int teamSize, int game_cap) {
        List<Team> teams = new ArrayList<>();
        List<Participant> unassignedParticipants = new ArrayList<>();

        if (listOfParticipants.isEmpty() || teamSize <= 0) return new TeamFormationResult(teams, listOfParticipants);

        int numberOfTeams = (int) Math.ceil((double) listOfParticipants.size() / teamSize);
        for (int j = 0; j < numberOfTeams; j++) {
            teams.add(new Team("Team" + (j + 1)));
        }

        // --- P3 Hard Constraint: Max 1 Leader Allocation ---
        List<Participant> allLeaders = listOfParticipants.stream()
                .filter(p -> "Leader".equals(p.getPersonalityType()))
                .sorted(Comparator.comparingDouble(Participant::getCompositeScore).reversed())
                .toList();

        List<Participant> nonLeaders = listOfParticipants.stream()
                .filter(p -> !"Leader".equals(p.getPersonalityType()))
                .toList();

        // Assign exactly one leader per team (if available)
        List<Participant> assignedLeaders = new ArrayList<>();
        for (int i = 0; i < numberOfTeams; i++) {
            if (i < allLeaders.size()) {
                teams.get(i).addPlayers(allLeaders.get(i));
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
        remainingPlayers.sort(Comparator.comparingDouble(Participant::getCompositeScore).reversed()); // Sort the combined pool

        // 3. Simplified Snake Draft for remaining players
        int teamIndex = 0;
        int direction = 1;

        Iterator<Participant> iterator = remainingPlayers.iterator();
        while (iterator.hasNext()) {
            Participant currentPlayer = iterator.next();
            Team currentTeam = teams.get(teamIndex);

            // Check team capacity and constraints
            boolean isLeader = "Leader".equals(currentPlayer.getPersonalityType());
            boolean hasSpace = currentTeam.getMembers().size() < teamSize;
            boolean gameCapMet = currentTeam.getGameCount(currentPlayer.getPreferredGame()) < game_cap;
            // Check P3 Constraint: Must NOT be a Leader if the team already has one
            boolean leaderConstraintMet = !isLeader || currentTeam.getPersonalityCount("Leader") < 1;

            if (hasSpace && gameCapMet && leaderConstraintMet) {
                currentTeam.addPlayers(currentPlayer);
                iterator.remove(); // Remove player from the draft pool once assigned
            }

            // Update snake draft index (regardless of assignment success, the draft continues)
            // If teamIndex hits the boundary, reverse direction
            if (teamIndex == numberOfTeams - 1 && direction == 1) {
                direction = -1;
            } else if (teamIndex == 0 && direction == -1) {
                direction = 1;
            }
            teamIndex += direction;
        }

        // Any players left in remainingPlayers could not be assigned due to constraints or capacity.
        unassignedParticipants.addAll(remainingPlayers);

        return new TeamFormationResult(teams, unassignedParticipants);
    }


    // Team Display Helper (Static utility method)
    public static void displayTeams(TeamFormationResult result) {
        System.out.println("\n*** FINAL TEAM ROSTERS ***");
        if (result.teams.isEmpty()) {
            System.out.println("No teams were successfully formed.");
            return;
        }

        for (Team team : result.teams) {
            double avgSkill = (team.getMembers().isEmpty()) ? 0 : team.getTotalSkill() / (double)team.getMembers().size();
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


    //swap between two teams
    public boolean isSwapSafe(Team teamA, Participant playerA, Team teamB, Participant playerB, int gameMax) {

        if (teamA == null || teamB == null || playerA == null || playerB == null) return false;

        // Simulates the swap and checks constraints P1 and P2 on the resulting teams.
        try {
            // 1. Simulating Team A after swap: PlayerA out, PlayerB in
            List<Participant> tempMembersA = new ArrayList<>(teamA.getMembers());
            if (!tempMembersA.remove(playerA)) return false;
            tempMembersA.add(playerB);
            Team tempTeamA = new Team(teamA.getTeamName(), tempMembersA);

            // 2. Simulating Team B after swap: PlayerB out, PlayerA in
            List<Participant> tempMembersB = new ArrayList<>(teamB.getMembers());
            if (!tempMembersA.remove(playerA)) return false;
            tempMembersB.add(playerA);
            Team tempTeamB = new Team(teamB.getTeamName(), tempMembersB);

            if (tempTeamA.getGameCount(playerB.getPreferredGame()) > gameMax) return false;
            if (tempTeamB.getGameCount(playerA.getPreferredGame()) > gameMax) return false;


            // Check P2 (Personality Mix) on tempTeamA
            if (!checkSingleTeamPersonalityMix(tempTeamA)) return false;
            // Check P2 (Personality Mix) on tempTeamB
            if (!checkSingleTeamPersonalityMix(tempTeamB)) return false;

            return true;

        } catch (Exception e) {
            System.err.println("ERROR in isSwapSafe check: " + e.getMessage());
            return false;
        }
    }


    // Helper for isSwapSafe (Checks P2 constraint on a single team)
    private boolean checkSingleTeamPersonalityMix(Team team) {

        if (team == null || team.getMembers().isEmpty()) return false;
        try {

            int leaderCount = team.getPersonalityCount("Leader");
            int thinkerCount = team.getPersonalityCount("Thinker");
            int balancedCount = team.getPersonalityCount("Balanced");
            int actualSize = team.getMembers().size();

            // Must have exactly 1 Leader
            if (leaderCount != 1) return false;

            // Must have 1 or 2 Thinkers
            if (thinkerCount < 1 || thinkerCount > 2) return false;

            // Ensure all players are classified (for simplicity, only Leader/Thinker/Balanced are expected)
            if (leaderCount + thinkerCount + balancedCount != actualSize) return false;

            return true;
        } catch (Exception e) {
            System.err.println("ERROR during P2 check for team " + team.getTeamName() + ": " + e.getMessage());
            return false;
        }
    }

    // ====================================================================================
    // 1. Check Game Variety (P1) - New implementation
    // ====================================================================================
    public List<Map<String, Object>> checkGameVariety(List<Team> teams, int gameMax) {
        List<Map<String, Object>> failedTeams = new ArrayList<>();
        if (teams == null) return failedTeams;
        for (Team team : teams) {
            try {
                Map<String, Long> gameCounts = team.getMembers().stream()
                        .collect(Collectors.groupingBy(Participant::getPreferredGame, Collectors.counting()));

                for (Map.Entry<String, Long> entry : gameCounts.entrySet()) {
                    if (entry.getValue() > gameMax) {
                        Map<String, Object> failure = new HashMap<>();
                        failure.put("Team", team);
                        failure.put("teamName", team.getTeamName());
                        failure.put("reason", "game_cap_violation");
                        failure.put("game", entry.getKey());
                        failedTeams.add(failure);
                        break; // Only need one violation per team
                    }
                }
            } catch (Exception e) {
                System.err.println("ERROR during P1 check for team " + team.getTeamName() + ": " + e.getMessage());
            }
        }
        return failedTeams;
    }


    // ====================================================================================
    // 3. Check Role Diversity (P3)
    // Constraint: 3 unique roles if size <= 5, 4 unique roles if size > 5
    // ====================================================================================
    public List<Map<String, Object>> checkRoleDiversity(List<Team> teams) {

        List<Map<String, Object>> failedTeams = new ArrayList<>();
        if (teams == null) return failedTeams;
        for (Team team : teams) {
            try {
                Set<String> uniqueRoles = team.getMembers().stream()
                        .filter(Objects::nonNull)
                        .map(Participant::getPreferredRole)
                        .collect(Collectors.toSet());

                int size = team.getMembers().size();
                int requiredRoles = size > 5 ? 4 : 3; // Rule from doc

                if (uniqueRoles.size() < requiredRoles && size > 0) {
                    Map<String, Object> failure = new HashMap<>();
                    failure.put("Team", team);
                    failure.put("teamName", team.getTeamName());
                    failure.put("reason", "P3: role_diversity_violation (Found: " + uniqueRoles.size() + ", Required: " + requiredRoles + ")");
                    failedTeams.add(failure);
                }
            }catch (Exception e) {
                System.err.println("ERROR during P3 check for team " + team.getTeamName() + ": " + e.getMessage());
            }
        }
        return failedTeams;
    }



    // ====================================================================================
    // 4. Check Skill Balance (P4)
    // Constraint: Team average skill must be within a threshold of the overall average.
    // ====================================================================================
    public List<Map<String, Object>> checkSkillBalance(List<Team> teams, double skillThreshold) {

        List<Map<String, Object>> failedTeams = new ArrayList<>();
        if (teams == null || teams.isEmpty()) return failedTeams;

        try {
            double totalSkillAllTeams = teams.stream().filter(Objects::nonNull).mapToInt(Team::getTotalSkill).sum();
            int totalPlayers = teams.stream().filter(Objects::nonNull).mapToInt(team -> team.getMembers().size()).sum();
            double overallAvg = (totalPlayers > 0) ? totalSkillAllTeams / totalPlayers : 0;

            for (Team team : teams) {
                int size = team.getMembers().size();
                double teamAvg = (size > 0) ? (double) team.getTotalSkill() / size : 0;
                double deviation = Math.abs(teamAvg - overallAvg);

                if (deviation > skillThreshold && size > 0) {
                    Map<String, Object> failure = new HashMap<>();
                    failure.put("Team", team);
                    failure.put("teamName", team.getTeamName());
                    failure.put("averageSkill", teamAvg);
                    failure.put("overallAvg", overallAvg);
                    failure.put("deviation", deviation);
                    failure.put("reason", "P4: skill_imbalance");
                    failedTeams.add(failure);
                }
            }
        }catch (Exception e) {
            System.err.println("ERROR during P4 skill calculation: " + e.getMessage());
        }
        return failedTeams;
    }




    //Iteratively optimizes the teams using concurrent processing to find the best swap in each round quickly.

    public void optimizeTeamsConcurrent(List<Team> teams) {

        // --- 1. ExecutorServ2ice Setup ---
        int poolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);

        System.out.printf("LOG: Initializing Optimization Thread Pool with %d workers.\n", poolSize);

        boolean improvementFound = true;
        int round = 1;

        // Continue looping as long as we find a beneficial swap
        while (improvementFound) {
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

                Team teamA = findTeamByName(teams, bestSwap.teamAName);
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










































