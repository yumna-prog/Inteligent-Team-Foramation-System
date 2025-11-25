package com.gameclub.team.service;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

import javax.sound.midi.InvalidMidiDataException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.stream.Collectors;
// create personalityClassifier object, participant list
//read the file line by line
// check missing values, incorrect format and out of range errors
// extract data and assign to respective participant attributes
// create participant object loading the extracted data
//add the created objects to the list


//File Handling
public class FileService implements FileServiceInt {


    private String file_path;
    private static String[] csv_header = {"ID","Name","Email","PreferredGame","SkillLevel","PreferredRole","PersonalityScore","PersonalityType"};
    private static final int max_persona_score = 25;

    public FileService(String file_path) {
        this.file_path = file_path;
    }

    public void writeSurveyDataToCSV(Participant participant) throws IOException {
        try (FileWriter fw = new FileWriter(file_path, true);
             PrintWriter pw = new PrintWriter(fw)) {


            //Write header if file empty
            File file = new File(file_path);
            if (file.length() == 0) {
                pw.println(String.join(",",csv_header));
            }
            double normalizedScore = participant.getNormalizedScore();


            //Construct the csv
            String csvLine = String.format("%s,%s,%s,%s,%d,%s,%.2f,%s",
                    participant.getPlayerId(),
                    participant.getName(),
                    participant.getEmail(),
                    participant.getPreferredGame(),
                    participant.getSkillLevel(),
                    participant.getPreferredRole(),
                    normalizedScore,
                    participant.getPersonalityType()
            );

            //write the csv line
            pw.println(csvLine);



        } catch (IOException e) {
            System.err.println(" Could not write data to CSV file" + e.getMessage());
        }
    }


    //UPLOAD THE DATA


    public List<Participant> loadParticipants() {
        List<Participant> participants = new ArrayList<>();
        final int fieldCount = 8; // only data from 7 fields are needed, the variable should be used across all objects
        ValidationService validator = new ValidationService(this.file_path);
        //ensure the file is closed soon after reading
        try (BufferedReader br = new BufferedReader(new FileReader(file_path))) {

            String line;
            boolean firstLine = true;
            //Read each line
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                String[] values = line.split(",");

                //check for missing fields -> how does it have to be handled
                if (values.length < fieldCount) {
                    System.err.println("Skipping invalid line: " + line);
                    continue;
                }
                try {
                    String playerId = values[0].trim();
                    String name = values[1].trim();
                    String email = values[2].trim();
                    String preferredGame = values[3].trim();
                    int skillLevel = Integer.parseInt(values[4].trim());
                    String preferredRole = values[5].trim();
                    double normalizedScore = Double.parseDouble(values[6].trim());
                    String personalityType = values[7].trim();

                    //Validate data
                    validator.validate_id(playerId,false);
                    validator.validate_name(name);
                    validator.validate_email(email);

                    if(!validator.validateSkillLevel(skillLevel)||
                    !validator.validateNormalizedScore(normalizedScore)) {
                        System.err.println("Validation Failed for line: " + line);
                        continue;
                    }

                    //Create Participant object using extracted data
                    Participant participant = new Participant(playerId, name, email, preferredGame, skillLevel, preferredRole,normalizedScore,personalityType);

                    //Add object to the list
                    participants.add(participant);

                }catch (NumberFormatException e){
                    System.err.println("Format error in line: " + line);
                }

            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found");

        } catch (IOException e) {
            System.out.println("Error reading file" + e.getMessage());
        }

        return participants;
    }

    //==================================SAVE THE FORMED TEAMS==================================//
    //Save the formed teams and the unassigned participants to a csv file

    public void saveFormedTeams(TeamFormationResult result, String fillPath) throws IOException {

        try(BufferedWriter writer =  new BufferedWriter(new FileWriter(file_path))){

            writer.write("==========================================================");
            writer.newLine();
            writer.write("           TEAM FORMATION REPORT - RESULTS");
            writer.newLine();
            writer.write("==========================================================");
            writer.newLine();
            writer.newLine();

            writer.write("--- FORMED TEAMS ---");
            writer.newLine();

            if(result.teams.isEmpty()) {
                writer.write("No teams were successfully formed.");
                writer.newLine();
            }
            else{
                for(Team team : result.teams){

                    double avgSkill = (team.getMembers().isEmpty()) ? 0 : team.getTotalSkill() / (double) team.getMembers().size();

                    writer.write("----------------------------------------------------------");
                    writer.newLine();
                    writer.write(String.format("TEAM: %s (Members: %d, Avg Skill: %.2f)",
                            team.getTeamName(), team.getMembers().size(), avgSkill));
                    writer.newLine();
                    writer.write("----------------------------------------------------------");
                    writer.newLine();

                    // Header for the member table
                    writer.write(String.format("%-15s | %-10s | %-12s | %-5s | %s",
                            "NAME", "ROLE", "PERSONA", "SKILL", "GAME"));
                    writer.newLine();
                    writer.write("----------------------------------------------------------");
                    writer.newLine();

                    //Write the member details
                    for(Participant p : team.getMembers()){
                        writer.write(String.format("%-15s | %-10s | %-12s | %-5d | %s",
                                p.getName(),
                                p.getPreferredRole(),
                                p.getPersonalityType(),
                                p.getSkillLevel(),
                                p.getPreferredGame()));


                        writer.newLine();
                    }
                    writer.newLine();
                }
            }

            writer.newLine();
            writer.write("==========================================================");
            writer.newLine();

            // --- 2. Write Unassigned Participants ---
            writer.write("--- UNASSIGNED PARTICIPANTS ---");
            writer.newLine();

            if (result.unassignedParticipants.isEmpty()) {
                writer.write("All participants were successfully assigned to a team.");
                writer.newLine();

            }else{
                writer.write("The following" + result.unassignedParticipants.size() +" participants could not be assigned due to constraints (Game Cap / Leader Limit / Capacity):");
                writer.newLine();

                // Header for the unassigned participant table
                writer.write(String.format("%-15s | %-10s | %-12s | %-5s | %s",
                        "NAME", "ROLE", "PERSONA", "SKILL", "GAME"));

                writer.newLine();
                writer.write("-----------------------------------------------------");
                writer.newLine();

                for(Participant p : result.unassignedParticipants){
                    writer.write(String.format("%-15s | %-10s | %-12s | %-5d | %s",
                            p.getName(),
                            p.getPreferredRole(),
                            p.getPersonalityType(),
                            p.getSkillLevel(),
                            p.getPreferredGame()));


                    writer.newLine();
                }
            }
            writer.write("==========================================================");
            writer.newLine();

        }catch(IOException e){
            System.out.println("Error saving the formed teams to a file: "+"Cause: " + e.getMessage());
            throw e;
        }
    }






































}


