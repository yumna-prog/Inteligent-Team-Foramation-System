package com.gameclub.team.controller;

import com.gameclub.team.model.Participant;
import com.gameclub.team.service.FileService;

import java.util.List;

public class OrganizerController {
    //Requirement to upload file data
    public List<Participant> uploadParticipantData(String filepath) {
        FileService fileService = new FileService(filepath);
        return fileService.loadParticipants();
    }

    //Initialize team formation


}
