package com.gameclub.team.service;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;

public interface ConstraintValidatorInt {

    public boolean isValid(Team team, Participant participant);
}
