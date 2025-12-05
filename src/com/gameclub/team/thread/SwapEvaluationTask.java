package com.gameclub.team.thread;
import com.gameclub.team.model.Participant;
import com.gameclub.team.model.Team;
import com.gameclub.team.service.ConstraintChecker;
import java.util.concurrent.Callable;

public class SwapEvaluationTask implements Callable<BestSwapInfo> {

    //Reference to the original objects
    private final Team teamA;
    private final Team teamB;
    private final Participant playerX; // From Team A
    private final Participant playerY; // From Team B
    private final ConstraintChecker checker;


    public static boolean T1_TEST_MODE = false;
    public static boolean T2_TEST_MODE = false;


    public SwapEvaluationTask(Team teamA, Team teamB, Participant playerX, Participant playerY, ConstraintChecker checker) {

        this.teamA = teamA;
        this.teamB = teamB;
        this.playerX = playerX;
        this.playerY = playerY;
        this.checker = checker;
    }

    //Executes the evaluation in a separate thread.
    @Override
    public BestSwapInfo call(){ /*6 seq*/

        if (T2_TEST_MODE && Math.random() < 0.005) {
            throw new RuntimeException("TEST FAIL: Simulated unexpected worker thread crash!");
        }


        //Calculate Original Score
        double originalScoreA = checker.evaluateTeamPenalty(teamA); /*6.1 seq*/
        double originalScoreB = checker.evaluateTeamPenalty(teamB);
        double originalTotalScore = originalScoreA + originalScoreB;

        //Perform Hypothetical Swap on deep copies
        Team tempA = teamA.deepCopy();/*7 seq*/
        Team tempB = teamB.deepCopy(); /*8 seq*/

        if (T1_TEST_MODE) {
            // Attempt to corrupt the copy
            Participant playerCopyX = tempA.getMemberByName(playerX.getName());
            if (playerCopyX != null) {
                playerCopyX.setSkill(0);

            }
        }


        Participant copyX = tempA.getMemberByName(playerX.getName());
        Participant copyY = tempB.getMemberByName(playerY.getName());

        if (copyX != null && copyY != null) {
            tempA.getMembers().remove(copyX);
            tempA.getMembers().add(copyY);
            tempB.getMembers().remove(copyY);
            tempB.getMembers().add(copyX);
        } else {

            return new BestSwapInfo(teamA.getTeamName(), teamB.getTeamName(), playerX.getName(), playerY.getName(), 0.0);
        }

        // Evaluate the New Team penalty score
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
