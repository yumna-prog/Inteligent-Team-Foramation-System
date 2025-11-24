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


public class Application {

     public static void main(String[] args) {

         Scanner sc = new Scanner(System.in);
         System.out.println("Welcome to Team Formation System");

         while (true) {
             try{
                 System.out.println("Select the role you represent");
                 System.out.println("1. Organizer");
                 System.out.println("2. Participant");
                 System.out.println("Enter your choice (1 or 2): ");

                 String input = sc.nextLine().trim();

                 //To validate the numeric output
                 int choice = Integer.parseInt(input);

                 switch (choice) {
                     case 1:
                         organizerFlow(sc);
                         return;

                     case 2:
                         participantFlow(sc);
                         return;

                     default:
                         System.out.println("Wrong choice. Try again");
                 }


             }
             catch(NumberFormatException e ){
                 System.out.println("Input must be an integer between 1 and 2. Please try again");

             }catch (Exception e){
                 System.out.println("Unexpected error: " + e.getMessage());
             }
         }




     }

         //Organizer workflow
         public static void organizerFlow(Scanner scanner) {

             OrganizerController orgController = new OrganizerController();
             List<Participant> participants = new ArrayList<>();
             boolean dataLoaded = false;

             while (true) {
                 System.out.print("\n--- Organizer Mode ---");
                 System.out.print("\nSelect your specific requirement:");
                 System.out.println("\n1. Upload participant data file");
                 System.out.println("2. Initiate Team Formation");
                 System.out.println("3. Exit Organizer Mode");
                 System.out.println("\nEnter your choice (1 , 2,3): ");

                 String choice = scanner.nextLine().trim();

                 switch (choice) {
                     case "1":
                         System.out.println("Enter the full path to the participant CSV file: ");
                         String fullPath = scanner.nextLine().trim();

                         // In this environment, we bypass the actual File check as the system uses mock data.
                         if (fullPath.isEmpty()) {
                             System.out.println("File path cannot be empty. Please try again.");
                             break;
                         }

                         System.out.println("Loading participant data from file... (Using internal mock data for demo)");

                         try {
                             // This uses the CSVParser with mock data
                             participants = orgController.uploadParticipantData(fullPath);
                             dataLoaded = true;

                             if (participants.isEmpty()) {
                                 System.out.println("Participant data could not be loaded as it doesn't contain valid participant records. Please try again.");
                                 dataLoaded = false;
                             } else {
                                 System.out.println("Participant data loaded successfully.");
                                 System.out.println("Loaded " + participants.size() + " participants");

                                 // CODE TO DISPLAY RESULTS ---
                                 System.out.println("\n--- Loaded Participants Snippet (First 5) ---");
                                 int displayLimit = Math.min(participants.size(), 5);
                                 for (int i = 0; i < displayLimit; i++) {
                                     Participant p = participants.get(i);
                                     System.out.printf("  - ID: %s, Name: %s, Game: %s, Skill: %d, Role: %s\n",
                                             p.getPlayerId(),
                                             p.getName(),
                                             p.getPreferredGame(),
                                             p.getSkillLevel(),
                                             p.getPreferredRole()
                                     );
                                 }
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
                         int team_size;

                         try{
                             String sizeInput = scanner.nextLine().trim();
                             if (sizeInput.isEmpty()) {
                                 System.out.println("Team size cannot be empty. Please try again.");
                                 break;
                             }

                             team_size = Integer.parseInt(sizeInput);

                             if(team_size < 2) {
                                 System.out.println("The minimum team size must be 2. Please try again.");
                                 break;
                             }
                         } catch (NumberFormatException e) {
                             System.out.println("Invalid input. Enter a numeric value for team size. Please try again.");
                             break;
                         }

                         // Call the controller method
                         TeamFormationResult result = orgController.initiateTeamFormation(participants, team_size);

                         // Display the results, using the static helper from TeamBuilder
                         TeamBuilder.displayTeams(result);

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
         public static void participantFlow(Scanner scanner) {
             System.out.print("\n--- Participant Mode ---");
             System.out.print("\nSelect your specific requirement:");
             System.out.println("\n1. Fill the participant Survey");
             System.out.println("2. View Formed Teams");
             System.out.println("\nEnter your choice (1 or 2): ");

             String choice = scanner.next().trim();

             switch(choice){
                 case "1":
                     System.out.println("\nPlease enter your details to proceed with the survey ");

                     SurveyController survey = new SurveyController();

                     try{
                         Participant player = survey.runSurvey();
                         System.out.println("\nSurvey has been successfully completed");
                         System.out.println("Survey details have been successfully saved");
                         survey.displaySurvey(player);
                     }catch(Exception e){
                         System.out.println("\nSurvey could not be completed " + e.getMessage());
                     }

                     break;



                 case "2":
                     System.out.println("===== Formed Teams =====");
                     //


                 default:
                     System.out.println("\nInvalid choice! Please select 1 or 2.");
             }


         }











}
