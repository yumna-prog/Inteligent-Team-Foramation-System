package com.gameclub.team.service;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

//Shows what the Validation Service class does
public interface ValidationServiceInt {

    public boolean isValid(Team team, Participant participant);
}
