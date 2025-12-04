package com.gameclub.team.thread;

import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;
import com.gameclub.team.service.ConstraintChecker;

import java.util.concurrent.Callable;

//defines the task that each worker thread will execute
//calculate the score improvement for a single swap
//works on the deep copies to avoid race conditions


public class SwapEvaluationTask implements Callable<BestSwapInfo> {

    //reference to the original objects
    private final Team teamA;
    private final Team teamB;
    private final Participant playerX; // From Team A
    private final Participant playerY; // From Team B
    private final ConstraintChecker checker;



    // --- TEMPORARY TEST FLAGS (MANUALLY TOGGLE) ---
    public static boolean T1_TEST_MODE = false; // For Deep Copy Integrity
    public static boolean T2_TEST_MODE = false; // For Thread Interruption
    // ---------------------------------------------


    public SwapEvaluationTask(Team teamA, Team teamB, Participant playerX, Participant playerY, ConstraintChecker checker) {

        this.teamA = teamA;
        this.teamB = teamB;
        this.playerX = playerX;
        this.playerY = playerY;
        this.checker = checker;
    }

    //Executes the evaluation in a separate thread.
     //return The BestSwapInfo object containing the calculated improvement score

    @Override
    public BestSwapInfo call() throws Exception { /*6 seq*/


        // T2: THREAD INTERRUPTION TEST HOOK
        if (T2_TEST_MODE && Math.random() < 0.005) { // Crash ~0.5% of tasks
            throw new RuntimeException("TEST FAIL: Simulated unexpected worker thread crash!");
        }


        //1. Calculate Original Score
        double originalScoreA = checker.evaluateTeamPenalty(teamA); /*6.1 seq*/
        double originalScoreB = checker.evaluateTeamPenalty(teamB);
        double originalTotalScore = originalScoreA + originalScoreB;

        //2. Perform Hypothetical Swap on deep copies
        Team tempA = teamA.deepCopy();/*7 seq*/
        Team tempB = teamB.deepCopy(); /*8 seq*/


        // T1: DEEP COPY INTEGRITY TEST HOOK
        if (T1_TEST_MODE) {
            // Attempt to corrupt the copy, which should NEVER affect the original
            Participant playerCopyX = tempA.getMemberByName(playerX.getName());
            if (playerCopyX != null) {
                // Change the skill level to an easily recognizable value (0)
                playerCopyX.setSkill(0);

            }
        }




        // Find the actual participant objects in the copies to swap by name
        Participant copyX = tempA.getMemberByName(playerX.getName());
        Participant copyY = tempB.getMemberByName(playerY.getName());

        if (copyX != null && copyY != null) {
            // Remove X from A, add Y to A
            tempA.getMembers().remove(copyX);
            tempA.getMembers().add(copyY);

            // Remove Y from B, add X to B
            tempB.getMembers().remove(copyY);
            tempB.getMembers().add(copyX);
        } else {
            // If the member cannot be found in the deep copy , return 0 improvement
            return new BestSwapInfo(teamA.getTeamName(), teamB.getTeamName(), playerX.getName(), playerY.getName(), 0.0);
        }

        // 3. Evaluate the New Team penalty score
        double newScoreA = checker.evaluateTeamPenalty(tempA);
        double newScoreB = checker.evaluateTeamPenalty(tempB);
        double newTotalScore = newScoreA + newScoreB;

        // the Improvement Score
        // Improvement = how much the PENALTY score went down
        double improvement = originalTotalScore - newTotalScore;

        return new BestSwapInfo( /*6.3 seq*/
                teamA.getTeamName(),
                teamB.getTeamName(),
                playerX.getName(),
                playerY.getName(),
                improvement
        );
    }
}
