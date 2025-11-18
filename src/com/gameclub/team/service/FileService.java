package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

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
    private static final String csv_header = "ID,Name,Email,PreferredGame,SkillLevel,PreferredRole,PersonalityScore,PersonalityType";
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
                pw.println(csv_header);
            }
            //Convert the raw ratings to get the data suitable for the csv
            int rawScore = participant.getPersonalityScore();
            double normalizedScore = (double) rawScore / max_persona_score * 100;


            //Construct the csv
            String csvLine = String.format("%s,%s,%s,%s,%d,%s,%d,%s",
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

            System.out.println("Successfully wrote" + participant.getName() + " data");


        } catch (IOException e) {
            System.err.println(" Could not write data to CSV file" + e.getMessage());
        }
    }


    //dummy method for the core logic
    public List<Participant> readAllParticipants() {
        List<Participant> participants = new ArrayList<>();


        return participants;

    }

    //UPLOAD THE DATA


    public List<Participant> loadParticipants() {
        List<Participant> participants = new ArrayList<>();
        final int fieldCount = 8; // only data from 7 fields are needed, the variable should be used across all objects
        ValidationService validator = new ValidationService();
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
                    validator.validate_id(playerId);
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
}


