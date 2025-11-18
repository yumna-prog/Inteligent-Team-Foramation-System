package com.gameclub.team.model;
import java.util.ArrayList;
import java.util.List;

//Represents a collection of members
public class Team {


    private String Name;
    //participant list
    //INDICATION OF AGGREGATION - the team has participants but the participants can exist without the team
    private final List<Participant> members;


    //Default initialization
    public Team() {
        this.members = new ArrayList<>(); // what creating a team object a list will be created to add the members
        this.Name = "Team";
    }

    //with name
    public Team(String name) {
        this.Name = name;
        this.members = new ArrayList<>();

    }

    public String getTeamName() {
        return Name;
    }
    public void setTeamName(String teamName) {
        Name = teamName;
    }

    //add participants
    public void addPlayers(Participant p) {
        members.add(p);

    }
    //remove player
    public void removePlayer(Participant p) {
        members.remove(p);
    }
    //get the participants
    public List<Participant> getMembers() {
        return members;
    }

    //Implement the method for lowest ranked player
    public Participant lowestRankedPlayerByGame(String game){
        Participant lowestPlayer = null;
        for(Participant p : members){
            if(p.getPreferredGame().equalsIgnoreCase(game)){
                if(lowestPlayer == null ||p.getCompositeScore() <lowestPlayer.getCompositeScore()){
                    lowestPlayer = p;

                }
            }
        }
        return lowestPlayer;
    }

    //Find the best player to be swapped
    public Participant FindBestSwapPlayer (String violatingGame, double targetScore) {
        // Initialize the best candidate
        Participant bestCandidate = null;

        //Initiate the minimum difference -> what do need the min difference
        double min_diff = Double.MAX_VALUE;

        // loop through the list of players who are not in failed teams
        for(Participant player : members ){
            //check if the player selected is not interested in the game
            if(!player.getPreferredGame().equalsIgnoreCase(violatingGame)){
                //calculate the absolute difference between the players score and the target
                double currentDifference = Math.abs(player.getCompositeScore()-targetScore);

                //if this player is better  match than the current best
                if(currentDifference < min_diff){

                    //update the minimum difference
                    min_diff = currentDifference;

                    //set this player as the new best candidate
                    bestCandidate = player;
                }
            }
        }
        return bestCandidate;
    }

    // find the lowest rank in each personality type

    //Implement the method for lowest ranked player
    public Participant personality_lowestRankedPlayer(String personality){
        Participant lowest_personalityPlayer = null;
        for(Participant p : members){
            if(p.getPersonalityType().equalsIgnoreCase(personality)){
                if(lowest_personalityPlayer == null ||p.getCompositeScore() < lowest_personalityPlayer.getCompositeScore()){
                    lowest_personalityPlayer = p;
                }

            }

        }
        return lowest_personalityPlayer;

    }


    //get the personality count for team
    public int getPersonalityCount(String personality){
        int personalityCount = 0;

        if(personality == null || personality.isBlank()){
            return 0;
        }
        for(Participant p : members){
            if(p.getPersonalityType().equalsIgnoreCase(personality)){
                personalityCount++;

            }

        }
        return personalityCount;
    }




}
