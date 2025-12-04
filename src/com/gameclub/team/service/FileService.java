package com.gameclub.team.service;
import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

//File Handling
//Abstraction
public class FileService implements FileServiceInt {


    private String file_path;
    private static String[] csv_header = {"ID", "Name", "Email", "PreferredGame", "SkillLevel", "PreferredRole", "PersonalityScore", "PersonalityType"};

    public FileService(String file_path) {
        this.file_path = file_path;
    }

    public void writeSurveyDataToCSV(Participant participant){
        try (FileWriter fw = new FileWriter(file_path, true);
             PrintWriter pw = new PrintWriter(fw)) {


            //Write header if file empty
            File file = new File(file_path);
            if (file.length() == 0) {
                pw.println(String.join(",", csv_header));
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

            pw.println(csvLine);


        } catch (IOException e) {
            System.err.println(" Could not write data to csv file" + e.getMessage());

        }
    }


    //Upload the data
    public List<Participant> loadParticipants() {
        List<Participant> participants = new ArrayList<>();
        final int fieldCount = 8; // only data from 7 fields are needed
        ValidationService validator = new ValidationService(this.file_path);

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

                //Check for missing fields -> how does it have to be handled
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

                    boolean valid = true;

                    try {
                        //Validate data
                        validator.validate_id(playerId, false);
                    }catch (Exception e) {
                        System.err.println("Invalid id");
                        valid = false;
                    }

                    try {
                        validator.validate_name(name);
                    }catch (Exception e) {
                        System.err.println("Invalid name");
                        valid = false;
                    }
                    try {
                        validator.validate_email(email);
                    }catch (Exception e) {
                        System.err.println("Invalid email");
                        valid = false;
                    }

                    if (!validator.validateSkillLevel(skillLevel) ||
                            !validator.validateNormalizedScore(normalizedScore)) {
                        System.err.println("Error: Validation failed");
                        continue;
                    }

                    //Create Participant object using extracted data
                    Participant participant = new Participant(playerId, name, email, preferredGame, skillLevel, preferredRole, normalizedScore, personalityType);

                    //Add object to the list
                    participants.add(participant);

                } catch (NumberFormatException e) {
                    System.err.println("Error: Format error");
                }catch (IllegalArgumentException e) {
                    System.err.println("Error :Problem reading participant file");
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
    // Each row includes the player details and the assigned Team name


    public void saveFormedTeams(TeamFormationResult result, String filePath) throws IOException {

        //Define the delimiter used in CSV files
        final String delimiter = ",";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("Team Name" + delimiter + "Name" + delimiter + "Role" + delimiter + "Personality" + delimiter + "Skill Level" + delimiter + "Preferred Game" + delimiter + "Composite Score");
            writer.newLine();

            for (Team team : result.teams) {

                for (Participant p : team.getMembers()) {

                    String csvRow = String.join(delimiter,
                            team.getTeamName(),
                            p.getName(),
                            p.getPreferredRole(),
                            p.getPersonalityType(),
                            String.valueOf(p.getSkillLevel()),
                            p.getPreferredGame()
                    );
                    writer.write(csvRow);
                    writer.newLine();

                }
            }
            //Write Unassigned Participants
            if (!result.unassignedParticipants.isEmpty()) {
               for (Participant p : result.unassignedParticipants) {
                    // No team name so unassigned
                    String csvRow = String.join(delimiter,
                            "UNASSIGNED",
                            p.getName(),
                            p.getPreferredRole(),
                            p.getPersonalityType(),
                            String.valueOf(p.getSkillLevel()),
                            p.getPreferredGame()
                    );
                    writer.write(csvRow);
                    writer.newLine();


                }
            }
        } catch (IOException e) {
            System.out.println("Error saving the formed teams to a file: " + "Cause: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //File Integrity checker helper method
    public static String calculateFileHash(String filePath){
        try(FileInputStream fis = new FileInputStream(filePath)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteBuffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fis.read(byteBuffer)) != -1) {
                digest.update(byteBuffer, 0, bytesRead);
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : digest.digest()) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            System.err.println("Error : File not found for hashing : " + filePath);
        }catch (IOException e){
            System.err.println("Issue while hashing file");
        }catch (NoSuchAlgorithmException e){
            System.err.println("Error: algorithm not available.");
        }
        return null;

    }
}


