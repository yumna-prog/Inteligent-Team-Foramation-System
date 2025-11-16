package com.gameclub.team.main;

import com.gameclub.team.controller.MainController;
import com.gameclub.team.service.ValidationService;
import com.sun.tools.javac.Main;

import javax.sound.midi.InvalidMidiDataException;
import java.time.chrono.MinguoChronology;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        //Survey interaction
        //1. Display the questions
        //create the current participant object
        //get the Validation service interface
        //ask the questions with try and catch

         Scanner scanner = new Scanner(System.in);
         System.out.println("==========================================");
         System.out.println("  PARTICIPANT SURVEY ");
         System.out.println("==========================================");

         //Participant main details
         System.out.print("\nEnter Player Name: ");
         String name = scanner.nextLine();
         System.out.print("\nEnter Player email(eg: user1@university.edu): ");
         String email = scanner.nextLine();
         System.out.print("\nEnter Participant playerId(eg: P001): ");
         String player_id = scanner.nextLine();

         //personality ranking
         int q1 = MainController.promptPersonalityRating("Q1. I enjoy taking the lead and guiding others during group activities.");
         int q2 = MainController.promptPersonalityRating("Q2. I prefer analyzing situations and coming up with strategic solutions.");
         int q3 = MainController.promptPersonalityRating("Q3. I work well with others and enjoy collaborative teamwork.");
         int q4 = MainController.promptPersonalityRating("Q4. I am calm under pressure and can help maintain team morale.");
         int q5 = MainController.promptPersonalityRating("Q5. I like making quick decisions and adapting in dynamic situations.");

         //Skill Level
         int skillLevel = MainController.promptForSelection("\n Select skill Level (1-10): How would you rate your skill?" ,1,10);


         //Select game  Interest
         int gameInterest = MainController.promptForSelection("""
                 
                  Select your preferred game (1-5):\
                 
                 1. Valorant\
                 
                 2. Dota\
                 
                 3. FIFA\
                 
                 4. Basketball\
                 
                 5. Badminton""",1,5);

         //Preferred role
         int role = MainController.promptForSelection("""
                 Select your preferred role (1-5):
                 
                 1. Strategist - Focuses on tactics and planning. Keeps the bigger picture in mind during gameplay \
                 
                 2. Attacker - Front line player. Good reflexes, offensive tactics, quick execution.\
                 
                 3. Defender - Protects and supports team stability. Good under pressure and team-focused\
                 
                 4. Supporter - Jack-of-all-trades. Adapts roles, ensures smooth coordination.\
                 
                 5. Coordinator - Communication lead. Keeps the team informed and organized in real time.\s""", 1,5);




























//        ValidationService validator = new ValidationService();
//
//        boolean validInput = false;
//        while (!validInput) {
//            System.out.println("Enter your choice from scale (1-5)");
//            System.out.println("Q1. I enjoy taking the lead and guiding others during group activities.");
//            String rawInput = scanner.nextLine();
//            try{
//                int score =validator.validateScore(rawInput,1,5);
//                // set the answer to the current participant
//                validInput = true;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//
//
//        }
//
//        System.out.println("Q1. I enjoy taking the lead and guiding others during group activities.");
//        System.out.println("Q2. I prefer analyzing situations and coming up with strategic solutions.");
//        System.out.println("Q3. I work well with others and enjoy collaborative teamwork.");
//        System.out.println("Q4. I am calm under pressure and can help maintain team morale.");
//        System.out.println("Q5. I like making quick decisions and adapting in dynamic situations.");
//
















        //2. A new Participant object is created to temporarily hold the answers as they are provided
        //3. The answers are validated through ValidationService
    }
}
