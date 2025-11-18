package com.gameclub.team.service;

import com.gameclub.team.model.Participant;

import java.io.IOException;
import java.util.List;
//Shows what the File Service class does
public interface FileServiceInt {

    public void writeSurveyDataToCSV(Participant participant) throws IOException;
    public List<Participant> loadParticipants();
}
