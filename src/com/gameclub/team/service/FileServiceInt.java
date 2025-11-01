package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

import java.util.List;

public interface FileServiceInt {

    public List<Participant> loadParticipants(String filepath);
}
