package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

import javax.sound.midi.InvalidMidiDataException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
// create personalityClassifier object, participant list
//read the file line by line
// check missing values, incorrect format and out of range errors
// extract data and assign to respective participant attributes
// create participant object loading the extracted data
//add the created objects to the list


//File Handling
public class FileService implements FileServiceInt {


    //dummy method for the core logic
    public List<Participant> readAllParticipants(){
        List<Participant> participants = new ArrayList<>();

        //add the participant objects to a list
        participants.add(new Participant("Alex","Valorant", 10, "Defender", "Leader", 19));
        participants.add(new Participant("Ben", "Dota",9,"Attacker", "Balanced", 17));
        participants.add(new Participant("Chris", "FIFA",8, "Midfielder", "Thinker", 15));
        participants.add(new Participant("David", "Valorant",7,  "Defender", "Balanced", 15));

        return participants;

    }

















    private final PersonalityClassifier classifier = new PersonalityClassifier();  // the personality can only be assigned once
    private static final int fieldCount = 8; // only data from 7 fields are needed, the variable should be used across all objects
// read lin by line
    //
    @Override
    public List<Participant> loadParticipants(String filepath) {
        List<Participant> participants = new ArrayList<>();
        int lineNumber = 0;

        //ensure the file is closed soon after reading
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {

            String line;
            //skip the header
            br.readLine();

            //Read each line
            while ((line = br.readLine()) != null) {
                lineNumber++;
                String[] values = line.split(",");

            //check for missing fields -> how does it have to be handled
                if (values.length < fieldCount) {
                    System.err.println("Structural Error.Missing data fields");
                    continue;
                }
                //check if skillLevel and personalityScore are numbers(format)
                int skillLevel;
                int personalityScore;
                String personalityType;


                try{
                    skillLevel = Integer.parseInt(values[4].trim());
                    personalityScore = Integer.parseInt(values[6].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Format error. SkillLevel or Score is not a number");
                    continue;
                }

                //Range error check
                try{
                    if(skillLevel < 1 || skillLevel > 10) {
                        throw new InvalidMidiDataException("Skill Level("+skillLevel+") is outside the range 1-10 range");
                                            }
                    if (personalityScore < 50 || personalityScore > 100) {
                        throw new InvalidMidiDataException("PersonalityScore"+personalityScore+" is outside the 50-100 range");

                    }
                } catch (InvalidMidiDataException e) {
                    System.err.println("Constraint Error"+ e.getMessage());
                }

                //Extract remaining field from CSV columns
                String playerId = values[0].trim();
                String preferredGame = values[3].trim();
                String preferredRole = values[5].trim();

                //Call the personalityClassifier to get the type
                personalityType = classifier.classify(personalityScore);

                //Create Participant object using extracted data
                //Participant participant = new Participant(playerId, preferredGame, skillLevel, preferredRole, personalityScore, personalityType);

                //Add object to the list
                //participants.add(participant);

            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found" +filepath);

        } catch (IOException e) {
            System.out.println("Error reading file" +e.getMessage());
        }

        return participants;
    }


}
