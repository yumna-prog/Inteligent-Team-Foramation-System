package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;

import com.gameclub.team.service.ConstraintChecker;
import com.gameclub.team.service.FileService;
import com.gameclub.team.service.TeamFormationResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class OrganizerController {
    //Requirement to upload file data
    private String filePath;

    public final TeamBuilder teamBuilder;

    public OrganizerController() {
        this.teamBuilder = new TeamBuilder();
    }

    public OrganizerController(TeamBuilder teamBuilder) {
        this.teamBuilder = teamBuilder;
    }
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
        //==================================================================================//
            //Set up ConstraintChecker for Optimization
            double globalSkillAvg = participants.stream()
                    .mapToInt(Participant::getSkillLevel)
                    .average().orElse(5.0);

            ConstraintChecker checker = new ConstraintChecker(globalSkillAvg);

            //Passing the checker instance to the TeamBuilder for the optimizer
            teamBuilder.setConstraintChecker(checker);
        //===============================================================================//

            // Step 1: Sort
            List<Participant> sortedParticipants = teamBuilder.sortParticipants(participants);
            System.out.println("LOG: Participants Sorted by Composite Score.");

            // Step 2: Form Teams (Drafting with initial constraints)
            TeamFormationResult result = teamBuilder.formTeams(sortedParticipants, teamSize, game_cap);
            System.out.println("LOG: Initial Teams Formed (Max 1 Leader Constraint applied).");

            // Step 3:  Concurrent applied for optimization
            System.out.println("\n--- Starting Iterative Team Optimization ---");

            teamBuilder.optimizeTeamsConcurrent(result.getTeams());
            System.out.println("LOG: Optimization phase executed successfully using concurrency.");

            final String output_filePath = "team formation results.csv";

            //Auto save the teams formed
            FileService fileService = new FileService();
            try{
                System.out.println("Saving the formed teams to file.........");
                //The result object contains the reference to the optimized tems
                fileService.saveFormedTeams(result,output_filePath);
                System.out.println("\nTeam successfully saved ");
            }
            catch(IOException e){
                System.out.println("Failed to automatically save team roster to CSV: ");
            }
            return result;

        } catch (Exception e) {
            System.err.println("ERROR during initiateTeamFormation execution: " + e.getMessage());
            e.printStackTrace();
            return new TeamFormationResult(Collections.emptyList(), participants);
        }
    }




}

