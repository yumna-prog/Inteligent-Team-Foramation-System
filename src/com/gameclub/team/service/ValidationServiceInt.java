package com.gameclub.team.service;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

import java.util.List;
import java.util.Map;

//Shows what the Validation Service class does
public interface ValidationServiceInt {

    //Validate survey data
    public String validate_id(String inputId);
    public String validate_email(String inputEmail);
    public int validateScore(String rawInput,int min, int max);

    //Validations for team formation
    //1.
//    public List<Map<String, Object>> checkGameCap(List<Team> teams, int gameMax);

    //2.



}
