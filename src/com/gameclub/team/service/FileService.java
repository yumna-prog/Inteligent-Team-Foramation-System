package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;


public class FileService implements FileServiceInt {

    @Override
    public List<Participant> loadParticipants(String filepath) {
        List<Participant> participants = new ArrayList<>();

        //ensure the file is closed soon after reading
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {

            String line;
            //skip the header
            br.readLine();

            //Read each line
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                //Extract field from CSV columns
                int playerId = Integer.parseInt(values[0].trim());
                String preferredGame = values[3].trim();
                int skillLevel = Integer.parseInt(values[4].trim());
                String preferredRole = values[5].trim();
                int personalityScore = Integer.parseInt(values[6].trim());
                String personalityType = values[7].trim();

                //Create Participant object using extracted data
                Participant participant = new Participant(playerId, preferredGame, skillLevel, preferredRole, personalityScore, personalityType);

                //Add object to the list
                participants.add(participant);

            }

        } catch (FileNotFoundException e) {
            System.out.println("File not found" +filepath);

        } catch (IOException e) {
            System.out.println("Error reading file" +e.getMessage());
        }

        return participants;
    }


}
