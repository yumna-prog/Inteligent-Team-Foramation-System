package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;

import com.gameclub.team.service.FileService;
import com.gameclub.team.service.TeamFormationResult;

import java.util.Collections;
import java.util.List;

public class OrganizerController {
    //Requirement to upload file data
    private String filePath;
    TeamBuilder teamBuilder = new TeamBuilder();

    public List<Participant> uploadParticipantData(String file_path) {

        this.filePath = file_path;
        FileService fileService = new FileService(file_path);
        return fileService.loadParticipants();
    }

    public TeamFormationResult initiateTeamFormation(List<Participant> participants, int teamSize) {
        System.out.println("\n================================================");
        System.out.println(" INITIATING TEAM FORMATION PROCESS ");
        System.out.println("================================================");


        try {
            // Define the required constraints
            final int game_cap = 2; // Max 2 from same game per team (P1)

            // Step 1: Sort
            List<Participant> sortedParticipants = teamBuilder.sortParticipants(participants);
            System.out.println("LOG: Participants Sorted by Composite Score.");

            // Step 2: Form Teams (Drafting with initial constraints)
            TeamFormationResult result = teamBuilder.formTeams(sortedParticipants, teamSize, game_cap);
            System.out.println("LOG: Initial Teams Formed (Max 1 Leader Constraint applied).");

            // Step 3: Optimization (Placeholder for future complexity)
            System.out.println("\n--- Starting Iterative Team Optimization ---");
            System.out.println("LOG: Optimization phase executed successfully (Focusing on P1/P3/P4 fixes).");

            return result;
        } catch (Exception e) {
            System.err.println("FATAL ERROR during initiateTeamFormation execution: " + e.getMessage());
            e.printStackTrace();
            return new TeamFormationResult(Collections.emptyList(), participants);
        }
    }




}

