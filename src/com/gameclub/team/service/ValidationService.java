package com.gameclub.team.service;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

//Exception Handling and Validating team formation
public class ValidationService{

    private static ValidationService instance;

    //Validate participant details -> Id, Name and email
    private String filePath;

    public ValidationService(String file_path) {
        this.filePath = file_path;
    }

    //Design pattern applied- Singleton
    public static ValidationService getInstance(String filePath) {
        if (instance == null) {
            instance = new ValidationService(filePath);
        }
        return instance;
    }

    public boolean idExists(String filePath, String id) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            boolean isFirstRow = true;

            while ((line = br.readLine()) != null) {

                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                String[] fields = line.split(",");

                if (fields.length > 0) {
                    String existingId = fields[0].trim();
                    if (existingId.equalsIgnoreCase(id)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file" + e.getMessage());
        }
        return false;

    }


    public String validate_id(String inputId,boolean checkForExistence) {

        String id_pattern = "^[A-Z]\\d{3}$";

        if (inputId == null || inputId.isEmpty()) {
            throw new IllegalArgumentException("The participant id cannot be empty "); //as the input length is not 4
        }
        //check if id follows the "P001" pattern
        if (!inputId.matches(id_pattern)) {
            throw new IllegalArgumentException("Invalid ID format (must be 1 letter + 3 digits, e.g., P001)");
        }
        //check if the id already exists
        if (checkForExistence) {
            if (idExists(filePath, inputId)) {
                throw new IllegalArgumentException("The participant id already exists");
            }

        }

        return inputId;

    }

    public void validate_name(String inputName) {
        if (inputName == null || inputName.trim().isEmpty()) {
            throw new IllegalArgumentException("The participant name cannot be empty");
        }
        if (!inputName.matches("^[A-Za-z0-9_]+$")) {
            throw new IllegalArgumentException("Name can only contain letters, numbers, and underscores)");
        }

    }


    //Validate email
    public String validate_email(String inputEmail) {
        String email_pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        if (inputEmail.isEmpty()) {
            throw new IllegalArgumentException("The participant email cannot be empty");
        }
        if (! inputEmail.matches(email_pattern)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return inputEmail;
    }


    // The validations for Survey data-> personality queries , interest and role selection
    // rate for each question be in (1-5) scale
    // the selecting of role should be in scale(1-5)
    //for the interest scale(1-5)
    public int validateScore(String rawInput, int min, int max) {
        int score;
        //Check if the input is an integer
        try {
            score = Integer.parseInt(rawInput.trim());
        } catch (NumberFormatException e) {
            throw new InputMismatchException("Input must be a whole number");

        }
        //Check if the input is within the scale of (1-5)
        if (score < min || score > max) {
            throw new InputMismatchException("Input must be between" + min + " and" + max);
        }
        return score;
    }

    public boolean validateSkillLevel(int inputSkillLevel) {
        return inputSkillLevel >= 1 && inputSkillLevel <= 10;
    }

    public boolean validateNormalizedScore(double score) {
        return score >= 50.0 && score <= 100;

    }
}
