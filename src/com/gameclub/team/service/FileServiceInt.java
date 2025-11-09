package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

import java.util.List;
//Shows what the File Service class does
public interface FileServiceInt {

    public List<Participant> loadParticipants(String filepath);
}
