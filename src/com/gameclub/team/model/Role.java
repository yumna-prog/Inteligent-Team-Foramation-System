package com.gameclub.team.model;

public enum Role {

    Strategist("Strategist - Focuses on tactics and planning. Keeps the bigger picture in mind during gameplay"),
    Attacker("Attacker - Front line player. Good reflexes, offensive tactics, quick execution."),
    Defender("Defender - Protects and supports team stability. Good under pressure and team-focused"),
    Supporter("Supporter - Jack-of-all-trades. Adapts roles, ensures smooth coordination."),
    Coordinator( "Coordinator - Communication lead. Keeps the team informed and organized in real time.");

    public String getDescription() {
        return description;
    }

    private final String description;

    Role(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return description; // for the survey display
    }

    public String getRole(){
        return this.name(); // to store in file
    }




}
