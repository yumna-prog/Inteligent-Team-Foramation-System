package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;
import com.gameclub.team.service.TeamFormationResult;

import java.sql.ClientInfoStatus;
import java.util.*;
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

//2. The participants will be sorted based on the composite score
    public List<Participant> sortParticipants(List<Participant> listOfParticipants) {
        if (listOfParticipants == null || listOfParticipants.isEmpty()) return Collections.emptyList();
        listOfParticipants.sort(Comparator.comparingDouble(Participant::getCompositeScore).reversed());
        return listOfParticipants;
    }

    public int calculateNumberOfTeams(List<Participant> listOfParticipants,int teamSize) {
        if (teamSize <= 0) return 0;
        return (int)  Math.ceil((double)listOfParticipants.size()/teamSize);
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
                .collect(Collectors.toList());

        List<Participant> nonLeaders = listOfParticipants.stream()
                .filter(p -> !"Leader".equals(p.getPersonalityType()))
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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

    public List<Map<String, Object>> fixGameVariety(List<Team> teams, int gameMax) {
        List<Map<String, Object>> failedTeams = checkGameVariety(teams, gameMax);
        List<Map<String, Object>> unresolvableIssues = new ArrayList<>();

        for (Map<String, Object> failure : new ArrayList<>(failedTeams)) {
            Team failingTeam = (Team) failure.get("Team");
            String overCapGame = (String) failure.get("game");

            try {
                // 1. Identify player to remove: Lowest skill player preferring the overCapGame
                Participant playerToRemove = failingTeam.getMembers().stream()
                        .filter(p -> p.getPreferredGame().equals(overCapGame))
                        .min(Comparator.comparingInt(Participant::getSkillLevel))
                        .orElse(null);

                if (playerToRemove == null) continue;

                Participant playerToSwap = null;
                Team swapTeam = null;

                // 2. Find a suitable player to swap in from another team
                for (Team candidateTeam : teams) {
                    if (candidateTeam.equals(failingTeam)) continue;

                    // Find a player in the candidate team that prefers a DIFFERENT game
                    Participant candidate = candidateTeam.getMembers().stream()
                            .filter(p -> !p.getPreferredGame().equals(overCapGame))
                            .min(Comparator.comparingInt(Participant::getSkillLevel)) // Choose lowest skill from swap team
                            .orElse(null);

                    if (candidate != null && isSwapSafe(failingTeam, playerToRemove, candidateTeam, candidate, gameMax)) {
                            playerToSwap = candidate;
                            swapTeam = candidateTeam;
                            break;
                        }
                    }

                if (playerToSwap != null && swapTeam != null) {
                    // Perform the safe swap
                    failingTeam.removePlayer(playerToRemove);
                    swapTeam.removePlayer(playerToSwap);

                    failingTeam.addPlayers(playerToSwap);
                    swapTeam.addPlayers(playerToRemove);

                    System.out.println("SWAP (P1 Game Fixed): " + playerToRemove.getName() + " (" + playerToRemove.getPreferredGame() + ") swapped from " + failingTeam.getTeamName() +
                            " with " + playerToSwap.getName() + " (" + playerToSwap.getPreferredGame() + ") from " + swapTeam.getTeamName());
                } else {
                    Map<String, Object> issue = new HashMap<>();
                    issue.put("Name", playerToRemove.getName());
                    issue.put("Reason", "P1 Failure (Game Cap) Unresolvable by Safe Swap.");
                    issue.put("TeamOrigin", failingTeam.getTeamName());
                    unresolvableIssues.add(issue);
                }
            }catch (Exception e) {
                System.err.println("ERROR during P1 fix for team " + failingTeam.getTeamName() + ": " + e.getMessage());
            }
        }
        return unresolvableIssues;
    }

    public List<Map<String, Object>> checkPersonalityMix(List<Team> teams) {

        List<Map<String, Object>> personaFailedTeams = new ArrayList<>();

        if (teams == null) return personaFailedTeams;
        for (Team team : teams) {
            if (!checkSingleTeamPersonalityMix(team)) {
                Map<String, Object> failure = new HashMap<>();
                failure.put("Team", team);
                failure.put("teamName", team.getTeamName());

                int leaderCount = team.getPersonalityCount("Leader");
                if (leaderCount != 1) failure.put("reason", leaderCount > 1 ? "P2: too_many_leaders" : "P2: no_leaders");
                else failure.put("reason", "P2: imbalance_thinker_or_unclassified");

                personaFailedTeams.add(failure);
            }
        }
        return personaFailedTeams;
    }


    public List<Map<String, Object>> fixPersonalityFailure(List<Map<String, Object>> failedTeams, List<Team> teams, int gameCap) {
        List<Map<String, Object>> unresolvableIssues = new ArrayList<>();
        failedTeams.forEach(failure -> {
            System.out.println("LOG: Attempting to fix P2 failure: " + failure.get("reason") + " in " + failure.get("teamName"));
            Map<String, Object> issue = new HashMap<>();
            issue.put("Name", "N/A");
            issue.put("Reason", failure.get("reason") + " (Fix logic placeholder - No swap attempted).");
            issue.put("TeamOrigin", failure.get("teamName"));
            unresolvableIssues.add(issue);
        });

        return unresolvableIssues;
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

    public List<Map<String, Object>> fixRoleDiversity(List<Team> teams, int gameMax) {

        List<Map<String, Object>> failedTeams = checkRoleDiversity(teams);
        List<Map<String, Object>> unresolvableIssues = new ArrayList<>();

        failedTeams.forEach(failure -> {
            System.out.println("LOG: Attempting to fix P3 failure: " + failure.get("reason") + " in " + failure.get("teamName"));
            Map<String, Object> issue = new HashMap<>();
            issue.put("Name", "N/A");
            issue.put("Reason", failure.get("reason") + " (Fix logic placeholder - No swap attempted).");
            issue.put("TeamOrigin", failure.get("teamName"));
            unresolvableIssues.add(issue);
        });

        return unresolvableIssues;
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

    public List<Map<String, Object>> fixSkillBalance(List<Team> teams, double skillThreshold, int gameMax) {

        List<Map<String, Object>> unresolvableIssues = new ArrayList<>();
        List<Map<String, Object>> failedTeams = checkSkillBalance(teams, skillThreshold);

        failedTeams.forEach(failure -> {
            System.out.println("LOG: Attempting to fix P4 failure: " + failure.get("reason") + " in " + failure.get("teamName"));
            Map<String, Object> issue = new HashMap<>();
            issue.put("Name", "N/A");
            issue.put("Reason", failure.get("reason") + " (Fix logic placeholder - No swap attempted).");
            issue.put("TeamOrigin", failure.get("teamName"));
            unresolvableIssues.add(issue);
        });
        return unresolvableIssues;
    }

    // ====================================================================================
    // CRITICAL: Optimization Loop (P1 -> P4)
    // ====================================================================================
    public List<Team> optimizeTeams(List<Team> teams, int gameCap, double skillThreshold) {

        final int MAX_ITERATIONS = 10;
        int currentIteration = 0;
        boolean constraintsBroken = true;

        List<Map<String, Object>> finalUnresolvableIssues = new ArrayList<>();

        while (currentIteration < MAX_ITERATIONS) {
            constraintsBroken = false;
            currentIteration++;
            System.out.println("\n--- Optimization Iteration " + currentIteration + " ---");
            int initialIssuesCount = finalUnresolvableIssues.size();

            // P1: Game Variety Check/Fix
            List<Map<String, Object>> p1Failures = checkGameVariety(teams, gameCap);
            if (!p1Failures.isEmpty()) {
                System.out.println("P1 (Game Variety) failed on " + p1Failures.size() + " teams. Attempting fix...");
                finalUnresolvableIssues.addAll(fixGameVariety(teams, gameCap));
                constraintsBroken = true;
            }

            // P2: Personality Mix Check/Fix
            List<Map<String, Object>> p2Failures = checkPersonalityMix(teams);
            if (!p2Failures.isEmpty()) {
                System.out.println("P2 (Personality Mix) failed on " + p2Failures.size() + " teams. Attempting fix...");
                finalUnresolvableIssues.addAll(fixPersonalityFailure(p2Failures, teams, gameCap));
                constraintsBroken = true;
            }

            // P3: Role Diversity Check/Fix
            List<Map<String, Object>> p3Failures = checkRoleDiversity(teams);
            if (!p3Failures.isEmpty()) {
                System.out.println("P3 (Role Diversity) failed on " + p3Failures.size() + " teams. Attempting fix...");
                finalUnresolvableIssues.addAll(fixRoleDiversity(teams, gameCap));
                constraintsBroken = true;
            }

            // P4: Skill Balance Check/Fix
            List<Map<String, Object>> p4Failures = checkSkillBalance(teams, skillThreshold);
            if (!p4Failures.isEmpty()) {
                System.out.println("P4 (Skill Balance) failed on " + p4Failures.size() + " teams. Attempting fix...");
                finalUnresolvableIssues.addAll(fixSkillBalance(teams, skillThreshold, gameCap));
                constraintsBroken = true;
            }

            // Check if any new unresolvable issues were generated this iteration
            if (finalUnresolvableIssues.size() == initialIssuesCount && !constraintsBroken) {
                System.out.println("\n--- All Constraints Met. Optimization Complete. ---");
                break;
            }
        }

        if (currentIteration >= MAX_ITERATIONS) {
            System.err.println("WARNING: Optimization loop terminated after " + MAX_ITERATIONS + " iterations without convergence.");
        }

        // --- FINAL FAILURE REPORT ---
        System.out.println("\n================================================");
        System.out.println("      FINAL CONSTRAINT FAILURE REPORT");
        System.out.println("================================================");
        if (finalUnresolvableIssues.isEmpty()) {
            System.out.println("SUCCESS: No unresolvable issues were logged during optimization.");
        } else {
            System.out.println("ATTENTION: " + finalUnresolvableIssues.size() + " issues remain that could not be resolved by automated swaps:");
            finalUnresolvableIssues.forEach(issue ->
                    System.out.println(" - " + issue.get("Reason") + " | Participant: " + issue.get("Name") + " | Team: " + issue.get("TeamOrigin")));
        }
        System.out.println("================================================");
        // --- END REPORT ---

        return teams;
    }

    public static  void displayTeams(List<Team> teams) {
        if (teams == null || teams.isEmpty()){
            System.out.println("No teams were formed or teams list is empty.");
            return;
        }
        System.out.println("\\n==========================================");
        System.out.println(" FINAL TEAMS LIST");
        System.out.println("\\n==========================================");


        for (Team currentTeam : teams) {
            currentTeam.displayTeamDetails();

        }
        System.out.println("Team formation process completed.");

    }


}










































