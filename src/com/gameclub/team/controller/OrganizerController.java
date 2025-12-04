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
    private FileService fileService; // interface usage

    public OrganizerController() {
        this.teamBuilder = new TeamBuilder();
        this.fileService = new FileService(filePath);
    }


    public List<Participant> uploadParticipantData(String file_path) {
        this.filePath = file_path;
        FileService fileService = new FileService(file_path);
        return fileService.loadParticipants();
    }

    public TeamFormationResult initiateTeamFormation(List<Participant> participants, int teamSize) { /* 2 - seq*/
        System.out.println("\n================================================");
        System.out.println(" INITIATING TEAM FORMATION PROCESS ");
        System.out.println("================================================");


        try {
            // Define the required constraints
            final int game_cap = 2; // Max 2 from same game per team (P1)

            //Sort by composite score
            List<Participant> sortedParticipants = teamBuilder.sortParticipants(participants); /* 2.1 - seq*/


            //Form Teams
            TeamFormationResult result = teamBuilder.formTeams(sortedParticipants, teamSize, game_cap); /* 2.3 - seq*/
            System.out.println("LOG: Initial Teams Formed (Max 1 Leader Constraint applied).");


            //Set up ConstraintChecker for Optimization
            double globalSkillAvg = participants.stream()
                    .mapToInt(Participant::getSkillLevel) /*3 seq*/
                    .average().orElse(5.0);

            ConstraintChecker checker = new ConstraintChecker(globalSkillAvg); /*4 seq*/

            //Passing the checker instance to the TeamBuilder for the optimizer
            teamBuilder.setConstraintChecker(checker); /*5 seq*/

            //Concurrent applied for optimization
            teamBuilder.optimizeTeamsConcurrent(result.getTeams()); /*2.4 seq*/
            System.out.println("LOG: Optimization phase executed successfully using concurrency.");

            final String output_filePath = "team formation results.csv";
            FileService fileService = new FileService(output_filePath); /*2.5*/

            try{
                System.out.println("Saving the formed teams to file.........");
                fileService.saveFormedTeams(result,output_filePath);   /*9*/
                System.out.println("\nSuccessfully saved ");
            }
            catch(IOException e){
                System.out.println("Failed to automatically save teams");
            }
            return result;

        } catch (Exception e) {
            System.err.println("Error during initiating team formation" + e.getMessage());
            e.printStackTrace();
            return new TeamFormationResult(Collections.emptyList(), participants);
        }
    }

}

