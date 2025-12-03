package com.gameclub.team.main;

import com.gameclub.team.controller.OrganizerController;
import com.gameclub.team.controller.SurveyController;
import com.gameclub.team.controller.TeamBuilder;
import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;
import com.gameclub.team.service.FileService;
import com.gameclub.team.service.TeamFormationResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.gameclub.team.service.FileService.calculateFileHash;


public class Application {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        System.out.println("\n==============================");
        System.out.println("   Welcome to TeamMate!   ");
        System.out.println("==============================");

        List<Participant> participants = new ArrayList<>();
        int team_size = 0;

        int choice = -1;

        while (true) {
            showMenu();
            String input = sc.nextLine().trim();
            //To validate the numeric output
            if (!input.matches("[0-9]+")) {
                System.out.println("Invalid choice.  Please enter a number (1 or 2).\n");
            }

            choice = Integer.parseInt(input);
            switch (choice) {
                case 1:
                    organizerFlow(sc);
                    return;

                case 2:
                    participantFlow(sc,participants,team_size);
                    return;

                default:
                    System.out.println("Wrong choice. Try again");
            }


        }



    }


         private static void showMenu(){

             System.out.println("  Select the role you represent");
             System.out.println("1. Organizer");
             System.out.println("2. Participant");
             System.out.println("Enter your choice (1 or 2): ");
         }



         //Organizer workflow
         public static void organizerFlow(Scanner scanner) {

             OrganizerController orgController = new OrganizerController(); /*1 seq*/
             List<Participant> participants = new ArrayList<>();
             boolean dataLoaded = false;

             while (true) {

                 System.out.println("\n================================");
                 System.out.print("        ORGANIZER MODE        ");
                 System.out.println("\n================================");
                 System.out.print("\nSelect your specific requirement:\n");
                 System.out.println("1. Upload participant data file");
                 System.out.println("2. Initiate Team Formation");
                 System.out.println("3. Return to Main flow");
                 System.out.println("\nEnter your choice (1 ,2 or 3): ");

                 String choice = scanner.nextLine().trim();

                 if (choice.equalsIgnoreCase("3")) {
                     System.out.println("Exiting Organizer Mode. Returning to main menu.");
                     return ;
                 }

                 switch (choice) {
                     case "1":
                         System.out.println("Enter the full path to the participant CSV file: ");
                         String fullPath = scanner.nextLine().trim();

                         // In this environment, we bypass the actual File check as the system uses mock data.
                         if (fullPath.isEmpty()) {
                             System.out.println("File path cannot be empty. Please try again.");
                             break;
                         }

                         // Step 1: Calculate hash
                         try {
                             String fileHash = calculateFileHash(fullPath);
                             System.out.println("Computed file hash: " + fileHash);

                             // Step 2: Compare with expected baseline (stored somewhere safe)
                             String expectedHash = "362c69e8ef9bed7f7c63e55ae23004cc1446d5cc81e70ec1cf3904cf34fbdf3f"; // baseline hash value
                             if (!fileHash.equalsIgnoreCase(expectedHash)) {
                                 System.out.println("WARNING: File integrity check failed. The file may have been altered.");
                                 break;
                             } else {
                                 System.out.println("File integrity verified ");
                             }
                         } catch (Exception e) {
                             System.out.println("Error computing file hash: " + e.getMessage());
                             break;
                         }
                         System.out.println("Loading participant data from file... (Using internal mock data for demo)");

                         try {
                             // This uses the CSVParser with mock data
                             participants = orgController.uploadParticipantData(fullPath);
                             dataLoaded = true;

                             if (participants == null || participants.isEmpty()) {
                                 System.out.println("The file contains no valid participant data.");
                                 dataLoaded = false;
                             }

                             else {
                                 System.out.println("Participant data loaded successfully.");
                                 System.out.println("Loaded " + participants.size() + " participants");

                                 System.out.println("\n-- Showing first 5 participants ---");
                                 participants.stream().limit(5).forEach(p ->
                                     System.out.printf("ID: %s | Name: %s | Game: %s | Skill: %d | Role: %s\n",
                                             p.getPlayerId(),
                                             p.getName(),
                                             p.getPreferredGame(),
                                             p.getSkillLevel(),
                                             p.getPreferredRole())
                                 );
                                 System.out.println("-------------------------------------------\n");

                             }

                         } catch (Exception e) {
                             System.out.println("Error while loading file: " + e.getMessage());
                             dataLoaded = false;
                         }

                         break;


                     case "2":

                         // TEAM FORMATION
                         if(participants.isEmpty() || !dataLoaded) {
                             System.out.println(" No participant data loaded. Please upload a CSV file first (Option 1).");
                             break;
                         }

                         System.out.println("\nEnter the desired team size");
                         try{
                             String sizeInput = scanner.nextLine().trim();

                             if (sizeInput.isEmpty()) {
                                 System.out.println("Team size cannot be empty. Please try again.");
                                 break;
                             }

                             int team_size = Integer.parseInt(sizeInput);

                             if(team_size < 2) {
                                 System.out.println("The minimum team size must be 2. Please try again.");
                                 break;
                             }

                             System.out.println("\nForming teams... please wait.");
                             TeamFormationResult result = orgController.initiateTeamFormation(participants, team_size);

                             TeamBuilder.displayTeams(result);

                         } catch (NumberFormatException e) {
                             System.out.println("Invalid input. Enter a numeric value for team size. Please try again.");

                         }
                         break;


                     case "3":
                         System.out.println("Exiting Organizer Mode. Returning to main menu.");
                         return;

                     default:
                         System.out.println("Wrong choice. Try again");
                 }

             }
         }

         //Participant workflow
         public static void participantFlow(Scanner scanner,List<Participant> participants, int team_size) {

             SurveyController survey = new SurveyController();
             OrganizerController orgController = new OrganizerController();



             System.out.println("\n================================");
             System.out.print("\n    PARTICIPANT MODE   ");
             System.out.println("\n================================");
             System.out.print("\nSelect your specific requirement:");
             System.out.println("\n1. Fill the participant Survey");
             System.out.println("2. View Formed Teams");
             System.out.println("3. Return to Main flow");
             System.out.println("\n---------------------------------");
             System.out.println("\nEnter your choice (1 , 2 or 3): ");

             String choice = scanner.next().trim();

             if (choice.equalsIgnoreCase("exit")||choice.equalsIgnoreCase("back")) {
                 System.out.println("Exiting Participant Mode. Returning to main menu.");
                 return ;
             }


             switch(choice){
                 case "1":
                     System.out.println("\nPlease enter your details to proceed with the survey ");


                     try{
                         Participant player = survey.runSurvey();
                         System.out.println("\nSurvey has been successfully completed");
                         survey.displaySurvey(player);
                     }catch(Exception e){
                         System.out.println("\nSurvey could not be completed " + e.getMessage());
                     }

                     break;



                 case "2":
                     System.out.println("\n===== Formed Teams =====");
                     // safety check
                     if (participants == null || participants.isEmpty()) {
                         System.out.println("❌ No participant data available. Teams cannot be displayed.");
                         break;
                     }

                     TeamFormationResult result = orgController.initiateTeamFormation(participants, team_size);

                     TeamBuilder.displayOnlyTeams(result);
                     break;

                 default:
                     System.out.println("\nInvalid choice! Please select 1 or 2.");
             }


         }











}