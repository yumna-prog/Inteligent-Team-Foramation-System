package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;
import com.gameclub.team.service.FileService;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SurveyController {

    //map the selections to sting values
    private static final String[] game_options = {"Valorant","Dota","FIFA","Basketball","Badminton"};
    private static final String[] role_options = {"Strategist", "Attacker", "Defender", "Supporter", "Coordinator"};
    private static final Scanner scanner = new Scanner(System.in);
    private static String file_path = "participants.txt";

    //GET PARTICIPANT DATA
    public Participant getParticipantData(){

        //Participant main details
        System.out.print("\nEnter Player Name: ");
        String name = scanner.nextLine();
        System.out.print("\nEnter Player email(eg: user1@university.edu): ");
        String email = scanner.nextLine();
        System.out.print("\nEnter Participant playerId(eg: P001): ");
        String player_id = scanner.nextLine();

        return  new Participant(name, email, player_id);

    }




    public void runSurvey() {

        //Survey interaction
        //1. Display the questions
        //create the current participant object
        //get the Validation service interface
        //ask the questions with try and catch

        //access data in the method above
        Participant basicParticipantData = getParticipantData();

        FileService  fileService = new FileService(file_path);


        System.out.println("==========================================");
        System.out.println("  PARTICIPANT SURVEY ");
        System.out.println("==========================================");

        //personality ranking
        System.out.print("\nPersonality Traits : Rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)");
        int q1 = SurveyController.promptPersonalityRating("Q1. I enjoy taking the lead and guiding others during group activities.");
        int q2 = SurveyController.promptPersonalityRating("Q2. I prefer analyzing situations and coming up with strategic solutions.");
        int q3 = SurveyController.promptPersonalityRating("Q3. I work well with others and enjoy collaborative teamwork.");
        int q4 = SurveyController.promptPersonalityRating("Q4. I am calm under pressure and can help maintain team morale.");
        int q5 = SurveyController.promptPersonalityRating("Q5. I like making quick decisions and adapting in dynamic situations.");

        //For personality score calculation
        List<Integer> personalityScore = Arrays.asList(q1,q2,q3,q4,q5);


        //Skill Level
        int skillLevel = SurveyController.promptForSelection("\n Select skill Level (1-10): How would you rate your skill?" ,1,10);


        //Select game  Interest
        int gameInterestIndex = SurveyController.promptForSelection("""
                      
                       Select your preferred game (1-5):\
                      
                      1. Valorant\
                      
                      2. Dota\
                      
                      3. FIFA\
                      
                      4. Basketball\
                      
                      5. Badminton""",1,game_options.length);

        String  game_option = game_options[gameInterestIndex-1];



        //Preferred role
        int roleIndex = SurveyController.promptForSelection("""
                      Select your preferred role (1-5):
                      
                      1. Strategist - Focuses on tactics and planning. Keeps the bigger picture in mind during gameplay \
                      
                      2. Attacker - Front line player. Good reflexes, offensive tactics, quick execution.\
                      
                      3. Defender - Protects and supports team stability. Good under pressure and team-focused\
                      
                      4. Supporter - Jack-of-all-trades. Adapts roles, ensures smooth coordination.\
                      
                      5. Coordinator - Communication lead. Keeps the team informed and organized in real time.\s""", 1,role_options.length);

        String  role_option = role_options[roleIndex-1];


        // DATA PROCESSING

        //CREATE player object
        Participant player =  new Participant(
                basicParticipantData.getPlayerId(),
                basicParticipantData.getName(),
                basicParticipantData.getEmail(),
                game_option,
                skillLevel,
                role_option,
                personalityScore
        );

        //Add participants to a list
        List<Participant> participants = new ArrayList<>();
        participants.add(player);


        //Save participant data
        try {
            fileService.writeSurveyDataToCSV(player);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }


    public static int promptPersonalityRating(String question){

       while(true){
           System.out.println(question + " -> Rating (1-5): ");

            try{
                String input = scanner.nextLine().trim();
                int rate = Integer.parseInt(input);
                if(rate >= 1 && rate <= 5){
                    return rate;
                }
                else{
                    System.out.println(" Error: Please enter a value between 1 and 5");
                }

            }catch(NumberFormatException e){
                System.out.println("Error: Invalid input ! Please enter a value between 1 and 5");
            }
        }

    }

    public static int promptForSelection(String message, int min, int max) {
        while(true){
            System.out.println(message);
            System.out.print("Enter selection ("+ min +" - "+ max +"):" );
            try{
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if(value >= min && value <= max){
                    return value;
                }
                System.out.println("Error : Enter a number between "+ min +" and "+ max);
            }
            catch (NumberFormatException e){
                System.out.println("Error: Invalid input. Please enter a numeric value");

            }

        }
    }









}
