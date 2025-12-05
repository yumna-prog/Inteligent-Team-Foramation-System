package com.gameclub.team.controller;
import com.gameclub.team.model.InterestGame;
import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Role;
import com.gameclub.team.service.FileService;
import com.gameclub.team.service.FileServiceInt;
import com.gameclub.team.service.PersonalityClassifier;
import com.gameclub.team.service.ValidationService;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SurveyController {

     private final Scanner scanner;
     private static String file_path = "participants.csv";


     // Default constructor for main application
     public SurveyController() {
         this(System.in);
     }

    public SurveyController(InputStream in) {
        this.scanner = new Scanner(in);
    }


    //Get participant data
    public Participant getParticipantData(){
        //Singleton applied//
        ValidationService validator = ValidationService.getInstance(file_path);

        String name;
        String email;
        String player_id;

        while(true) {
            try {
                System.out.print("\nEnter Player Name: ");
                name = scanner.nextLine();
                validator.validate_name(name);
                break;

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }


        while(true) {
            try {

                System.out.print("\nEnter Player email: ");
                email = scanner.nextLine();
                validator.validate_email(email);
                break;

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }



        while(true) {
            try {

                System.out.print("\nEnter Participant playerId(eg: P001): ");
                player_id = scanner.nextLine();
                validator.validate_id(player_id,false);
                break;

            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        return  new Participant(player_id,name,email);

    }




    public Participant runSurvey() {
        //Survey interaction
        //Display the questions
        //Create the current participant object
        //Get the Validation service interface
        //Ask the questions with try and catch

        try {
            Participant basicParticipantData = getParticipantData();
            //Interface usage
            FileServiceInt fileService = new FileService(file_path);


            System.out.println("\n==========================================");
            System.out.println("  PARTICIPANT SURVEY ");
            System.out.println("==========================================");

            //personality ranking
            System.out.print("\nPersonality Traits : Rate each statement from 1 (Strongly Disagree) to 5 (Strongly Agree)\n");
            int q1 = this.promptPersonalityRating("\nQ1. I enjoy taking the lead and guiding others during group activities.");
            int q2 = this.promptPersonalityRating("Q2. I prefer analyzing situations and coming up with strategic solutions.");
            int q3 = this.promptPersonalityRating("Q3. I work well with others and enjoy collaborative teamwork.");
            int q4 = this.promptPersonalityRating("Q4. I am calm under pressure and can help maintain team morale.");
            int q5 = this.promptPersonalityRating("Q5. I like making quick decisions and adapting in dynamic situations.");

            //For personality score calculation
            List<Integer> personalityRatings = Arrays.asList(q1, q2, q3, q4, q5);
            int rawScore = personalityRatings.stream().mapToInt(Integer::intValue).sum();
            double normalizedScore = (double) rawScore / 25 * 100;

            PersonalityClassifier classifier = new PersonalityClassifier();
            String personalityType = classifier.classify(normalizedScore);

            //Skill Level
            int skillLevel = this.promptForSelection("\n Select skill Level: How would you rate your skill?", 1, 10);


            //Select game  Interest
            InterestGame[] gamesOptions = InterestGame.values();
            System.out.println("\nSelect your preferred game (1-5):");
            for (int i = 0; i < gamesOptions.length; i++) {
                System.out.println((i + 1) + ". " + gamesOptions[i]);
            }
            int gameInterestIndex = this.promptForSelection("", 1, gamesOptions.length);
            InterestGame selectedGame = gamesOptions[gameInterestIndex - 1];
            String game_option = selectedGame.toString();

            //Preferred role
            Role[] role_options = Role.values();
            System.out.println("\nSelect your preferred role (1-5):");
            for (int i = 0; i < role_options.length; i++) {
                System.out.println((i + 1) + ". " + role_options[i]);
            }
            int roleIndex = this.promptForSelection("", 1, role_options.length);
            Role selectedRole = role_options[roleIndex - 1];


            String role_option_csv = selectedRole.getRole();


            // Data processing
            //Create player object
            Participant player = new Participant(
                    basicParticipantData.getPlayerId(),
                    basicParticipantData.getName(),
                    basicParticipantData.getEmail(),
                    game_option,
                    skillLevel,
                    role_option_csv,
                    normalizedScore,
                    personalityType
            );

            //Add participants to a list
            List<Participant> participants = new ArrayList<>();
            participants.add(player);


            //Save participant data
            try {
                fileService.writeSurveyDataToCSV(player);
            } catch (IOException e) {
                System.err.println("Error: Could not save participant data to file. Cause: " + e.getMessage());
            }

            return player;
        } catch (Exception e) {
            System.err.println("Unexpected error during survey" + e.getMessage());
            return null;
        }
    }


    public int promptPersonalityRating(String question){
         int attempts = 0;
         final int maxAttempts = 5;

       while(attempts < maxAttempts) {
           System.out.println(question);
           System.out.print("\nEnter a rating (1-5) ");

            try{
                String input = scanner.nextLine().trim();
                if(input.equals("exit")){
                    System.out.println("Survey cancelled by user");
                    return -1;
                }

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
            attempts++;
        }
       System.out.println("Too many invalid attempts.");
       return 3;

    }

    public int promptForSelection(String message, int min, int max) {
        int attempts = 0;
        final int maxAttempts = 3;

        while(attempts < maxAttempts) {
            System.out.println(message);
            System.out.print("Enter selection ("+ min +" - "+ max +"):" );
            try{
                String input = scanner.nextLine().trim();
                if(input.equals("exit")){
                    System.out.println("Survey cancelled by user");
                    return -1;
                }

                int value = Integer.parseInt(input);
                if(value >= min && value <= max){
                    return value;
                }else {
                    System.out.println("Error: Enter a number between" + min + " and " + max
                            + ". Attempts left: " + (maxAttempts - attempts - 1));
                }

            }
            catch (NumberFormatException e){
                System.out.println("Error: Invalid input. Please enter a numeric value");

            }
            attempts++;

        }
        System.out.println("Too many invalid attempts.");
        return min;
    }

    public void displaySurvey(Participant p){
        System.out.println("\n====================================================");
        System.out.println("     \uD83C\uDF89 SURVEY COMPLETED \uD83C\uDF89       ");
        System.out.println("\n====================================================");

        System.out.println("Here is your submitted information:");
        System.out.println("------------------------------------------");
        System.out.println("Player ID          : " +p.getPlayerId() );
        System.out.println("Player Name        : " +p.getName() );
        System.out.println("Player Email       : " +p.getEmail() );
        System.out.println("Player Skill Level : " +p.getSkillLevel() );
        System.out.println("Preferred Game     : " + p.getPreferredGame());
        System.out.println("Preferred Role     : " +p.getPreferredRole() );
        System.out.println("Personality Score  : " + p.getPersonalityScore() );
        System.out.println("Personality Type    : " + p.getPersonalityType() );
        System.out.println("------------------------------------------------");

    }








}
