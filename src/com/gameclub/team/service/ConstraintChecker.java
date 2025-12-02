package com.gameclub.team.service;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

import java.util.Map;
import java.util.stream.Collectors;


//purpose of class
//1. This is the service class that the concurrent tasks call to calculate the penalty score
//2.  Take a team object and return a penalty score
    //a. A low score (close to 0) is good and means the team respects the constraints.
    //b. A high score is bad and means the team violates several constraints
//The optimization along with threads are done to minimize the penalty score by finding the best swap


public class ConstraintChecker {



    // Global constant for the target average skill across ALL team
    // calculated dynamically based on all participants' average skill.
    private final double targetGlobalAverageSkill;


    public ConstraintChecker(double globalAvgSkill) {
        this.targetGlobalAverageSkill = globalAvgSkill;
    }


    public double getTargetGlobalAverageSkill() {
        return targetGlobalAverageSkill;
    }


    //The main method called by the SwapEvaluationTask to get a composite penalty score.
    public double evaluateTeamPenalty(Team team) {
        if (team.getMembers().isEmpty()) return 1000.0; // Heavy penalty for empty teams
        double penalty = 0.0;

        // Personality Mix Penalty
        // (1 Leader, 1-4 Thinkers)
        penalty += calculatePersonalityPenalty(team);

        // Game Variety Penalty
        // (Max 2 players from the same game per team)
        penalty += calculateGameVarietyPenalty(team);

        //  Role Diversity Penalty
        // (Ensure at least 3 different roles per team)
        penalty += calculateRoleDiversityPenalty(team);

        // Skill Balance Penalty
        // (Team average skill should be close to global average)
        penalty += calculateSkillBalancePenalty(team);

        return penalty;
    }

    //penalty based on Personality Mix (Leader, Thinker, Balanced).
    private double calculatePersonalityPenalty(Team team) {
        long leaders = countPersonalityType(team, "Leader");
        long thinkers = countPersonalityType(team, "Thinker");

        double penalty = 0.0;
        final int leader_cap = 1;

        if (leaders == 0) {
            penalty += 100.0; // Heavy penalty for no Leader
        } else if (leaders > leader_cap) {
            penalty += 50.0 * (leaders - 1); // Penalty for having too many Leaders
        }

        if (thinkers == 0) {
            penalty += 80.0; // Heavy penalty for no Thinker
        } else if (thinkers > 4) {
            penalty += 40.0 * (thinkers - 4); // Penalty for having too many Thinkers
        }

        return penalty;
    }

    //penalty based on Game Variety.

    private double calculateGameVarietyPenalty(Team team) {
        // Find counts of players by their preferred game
        Map<String, Long> gameCounts = team.getMembers().stream()
                .collect(Collectors.groupingBy(Participant::getPreferredGame, Collectors.counting()));

        double penalty = 0.0;
        final int MAX_GAME_CAP = 2;

        // Penalty for violating the cap
        for (Long count : gameCounts.values()) {
            if (count > MAX_GAME_CAP) {

                penalty += 20.0 * (count - MAX_GAME_CAP);
            }
        }
        return penalty;
    }

     //Calculates penalty based on Role Diversity.

    private double calculateRoleDiversityPenalty(Team team) {
        // Count the number of unique roles
        long uniqueRoles = team.getMembers().stream()
                .map(Participant::getPreferredRole)
                .distinct()
                .count();

        final int MIN_ROLES = 3;

        if (uniqueRoles < MIN_ROLES) {
            // High penalty for each role missing from the minimum requirement
            return 30.0 * (MIN_ROLES - uniqueRoles);
        }
        return 0.0;
    }

    //penalty based on Skill Balance
    private double calculateSkillBalancePenalty(Team team) {
        double teamAverage = team.getAverageSkill();

        // Calculate the squared difference from the target global average skill
        double deviation = teamAverage - this.targetGlobalAverageSkill;

        return 5.0 * (deviation * deviation);
    }

    private long countPersonalityType(Team team, String type) {
        return team.getMembers().stream()
                .filter(p -> p.getPersonalityType().equals(type))
                .count();
    }

    //Calculate the penalty score for a single team based on the constraints



}
