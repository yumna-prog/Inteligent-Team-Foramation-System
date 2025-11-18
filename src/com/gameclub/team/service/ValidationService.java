package com.gameclub.team.service;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

//Exception Handling and Validating team formation
public class ValidationService implements ValidationServiceInt {

    //Validate participant details -> Id, Name and email
    private String file_path;

    public boolean idExists(String filePath, String id){
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))){

            String line;
            boolean isFirstRow = true;

              while ((line = br.readLine()) != null){

                if(isFirstRow){
                    isFirstRow = false;
                    continue;
                }
                String[] fields = line.split(",");

                if(fields.length >0 ){
                    String existingId = fields[0].trim();
                    if(existingId.equalsIgnoreCase(id)){
                        return true;
                    }
                }
            }
        }
        catch (IOException e){
            System.err.println("Error reading file" + e.getMessage());
        }
        return false;

    }










    public String validate_id(String inputId) {

        String id_pattern = "^[A-Z]\\d{3}$";

        if (inputId == null || inputId.isEmpty()) {
            throw new IllegalArgumentException("The participant id cannot be empty "); //as the input length is not 4
        }
        //check if id follows the "P001" pattern
        if (!id_pattern.matches(id_pattern)) {
            throw new  IllegalArgumentException("Invalid ID format (must be 1 letter + 3 digits, e.g., P001)");
        }
        //check if the id already exists
        if (idExists(file_path, inputId)) {
            throw new  IllegalArgumentException("The participant id already exists");
        }

        return inputId;

    }

    public void validate_name(String inputName) {
        if (inputName == null || inputName.trim().isEmpty()) {
            throw new IllegalArgumentException("The participant name cannot be empty");
        }
        if (!inputName.matches("^[A-Za-z]+$")) {
            throw new IllegalArgumentException("Name can only contain letters and spaces)");
        }

    }





    //Validate email
    public String validate_email(String inputEmail) {
        String email_pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (inputEmail.isEmpty()) {
            throw new IllegalArgumentException("The participant email cannot be empty");
        }
        if (!email_pattern.matches(email_pattern)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return inputEmail;
    }



    // The validations for Survey data-> personality queries , interest and role selection
    // rate for each question be in (1-5) scale
    // the selecting of role should be in scale(1-5)
    //for the interest scale(1-5)
    public int validateScore(String rawInput,int min, int max){
        int score;
        //Check if the input is an integer
        try{
            score = Integer.parseInt(rawInput.trim());
        }
        catch(NumberFormatException e){
            throw new InputMismatchException("Input must be a whole number");

        }
        //Check if the input is within the scale of (1-5)
        if(score < min || score > max){
            throw new InputMismatchException("Input must be between"+min+" and"+ max);
        }
        return score;
    }

    public boolean validateSkillLevel(int inputSkillLevel){
        return inputSkillLevel >= 1 && inputSkillLevel <= 10;
    }

    public boolean validateNormalizedScore(double score){
        return score >= 50.0 && score <= 100;

    }

























    //Team formation constraint handling

//    //4. Check for the constraints
//    // The algorithm checks a constraint, and if it fails, it fixes the failure, and then checks all constraints again until every rule is met.
//    //-> IMPORTANT -> games that exceed the count by more than one
//    //-> more than one game group exceed th cap
//
//
//    public List<Map<String, Object>> checkGameCap(List<Team> teams, int gameMax) {
//
//        //create list to store teams that failed the validation with the information
//        List<Map<String,Object>> failedTeams = new ArrayList<>();
//
//
//        //Create a map to store count of players for each game
//        for (Team team : teams) {
//            HashMap<String, Integer> gameCountMap = new HashMap<>(); // how many players for each game in each team
//
//            //count the players to each game of the current team
//            for(Participant player : team.getMembers()){
//                String gameName = player.getPreferredGame();
//                gameCountMap.put(gameName, gameCountMap.getOrDefault(gameName, 0) + 1);
//            }
//
//            //Compare the count for the most common game against the defined cap
//            // consider other limitations
//            for(Map.Entry<String, Integer> entry : gameCountMap.entrySet()){
//                String gameName = entry.getKey();
//                int count = entry.getValue();
//
//                if(count > gameMax){
//                    Map<String,Object> failure = new HashMap<>();
//                    failure.put("Team",team);
//                    failure.put("gameName",gameName);
//                    failedTeams.add(failure); // can add information on how each team violates the rule
//
//                    break; //move to the next team
//
//                }
//
//            }
//        }
//        return failedTeams;
//    }
//    //IF FAILS -> what happens to the stored teams
//    //1. Identify the player to be removed -> this is the player with the lowest rank(to reduce the impact of avg skill) in the falling team
//
//    public void fixGameCapFailure(List<Map<String,Object>> failedTeams,List<Team> teams){
//        for(Map<String,Object> failedT : failedTeams){
//            Team failedTeam  = (Team)failedT.get("team"); // get the current team from the filed teams
//            String violatingGame = (String) failedT.get("violatingGame");
//
//            //find the suitable player to be removed
//            Participant playerToRemove = failedTeam.lowestRankedPlayerByGame(violatingGame);
//
//            //Identify the target player to be swapped
//            Participant playerToSwap = null;
//            Team swapTeam = null;
//            for(Team Passedteam : teams){
//                if (!Passedteam.equals(failedTeam)) {
//                    Participant candidate =  Passedteam.FindBestSwapPlayer(violatingGame, playerToSwap.getCompositeScore());
//                    //to ensure the swap doesn't violate the cap on the passed teams
//                    if(candidate != null){
//                        playerToSwap = candidate;
//                        swapTeam = Passedteam;
//                        break;
//                    }
//                }
//
//            }
//            //Perform Swap
//            if(playerToSwap != null){
//                failedTeams.remove(playerToRemove);
//                failedTeam.addPlayers(playerToSwap);
//
//                swapTeam.removePlayer(playerToSwap);
//                swapTeam.addPlayers(playerToRemove);
//
//                //debugging statement
//                System.out.println(
//                        "SWAP: " + playerToRemove.getName() + " (" + violatingGame +
//                                ") swapped from " + failedTeam.getTeamName() +
//                                " with " + playerToSwap.getName() + " (" + playerToSwap.getPreferredGame() +
//                                ") from " + swapTeam.getTeamName()
//                );
//
//            } else {
//                // No suitable swap found
//                System.err.println(
//                        "CRITICAL: Could not fix Game Cap failure on team " +
//                                failedTeam.getTeamName()
//                );
//
//
//            }
//
//        }
//    }
//
//    //Check PersonalityMix
//    public List<Team> checkPersonalityMix(List<Team> teams) {
//
//        //store teams that fail in personality mix
//        List<Team> personaFailedTeams =  new ArrayList<>();
//
//        //count how many unique personalities in each time
//        for  (Team team : teams) {
//            int leaderCount = team.getPersonalityCount("Leader");
//            int thinkerCount = team.getPersonalityCount("Thinker");
//
//            String failureType = null; //
//            //critical failure
//            if(leaderCount == 0)  {
//                failureType = "No_leaders";
//            }
//            else if(leaderCount > 1)  {
//                failureType = "too_many_leaders";
//            }
//            else if(thinkerCount == 0 || thinkerCount > 2)  {
//                failureType = "imbalance_thinker";
//
//            }
//            if(failureType != null) {
//                personaFailedTeams.add(team); // LATER HOW TO ADD THE TYPE
//            }
//
//        }
//        return personaFailedTeams;
//
//
//    }
//    //se if any fixation affects the already fixed constraints
//    public boolean canAcceptSwap(Participant playerRemoved, Participant playerInserted) {
//        //for testing
//        Participant tempPlayers;           //TO BE CONTINUED
//
//
//    }
//
//
//
//
//
//
//
//
//
//
//
//
//    public void fixPersonalityFailure(List<Map<String,Object>> failedTeams,List<Team> teams){
//
//        //give priority to fixing "Leader"
//        for(Map<String,Object> failure : failedTeams){
//           Team failingTeam  = (Team)failure.get("team");
//            if (failure.get("type") =="Leader") {
//
//                // Remove the lowest-ranked 'Balanced' or 'Thinker' player from the failing team
//                Participant playerToRemove = failingTeam.personality_lowestRankedPlayer("Leader");
//
//                Participant playerToSwap = null;
//
//                for (Team Passedteam : teams){
//                    if(Passedteam.getPersonalityCount("Leader") > 1){
//
//                        Participant candidate  = Passedteam.personality_lowestRankedPlayer("Leader");
//
//                        //Check if the swap won't violate Game Cap or Role Diversity
//
//                    }
//                }
//
//            }
//
//        }
//    }












    //b. Check for role diversity -
    //initialize the minimum unique roles per team
    // iterate  through each team and count how many unique roles
    // compares the count against the minimum requirement
    //IF FAILS ->

//    public  List<Map<String, Object>> checkSkillBalance(List<Team> teams, double skillThreshold) {
//        //Calculate the average skill score  for every final team
//        List<Double> averageSkills = new ArrayList<>();
//
//        for (Team team : teams) {
//            int totalSkill = 0;
//            for (Participant player : team.getMembers()) {
//                totalSkill = totalSkill + player.getSkillLevel();
//            }
//        }
//
//    }





}
