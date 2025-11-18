package com.gameclub.team.service;

import com.gameclub.team.controller.SurveyController;

public class SurveyTaskThread implements Runnable {

    private SurveyController surveyController;

    public SurveyTaskThread(SurveyController surveyController) {
        this.surveyController = surveyController;
    }
    @Override
    public void run() {
        surveyController.runSurvey();
    }

    //To be implemented in main method
//    public class MainApp {
//        public static void main(String[] args) {
//            SurveyController controller = new SurveyController();
//            ExecutorService executor = Executors.newFixedThreadPool(3); // 3 threads
//
//            for (int i = 0; i < 3; i++) {
//                executor.submit(new SurveyTask(controller));
//            }
//
//            executor.shutdown(); // Gracefully shut down after tasks complete
//        }
//    }


}
