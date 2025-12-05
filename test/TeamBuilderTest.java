import com.gameclub.team.controller.TeamBuilder;
import com.gameclub.team.model.Participant;
import com.gameclub.team.service.TeamFormationResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamBuilderTest {

    public static void main(String[] args) {
        TeamBuilder builder = new TeamBuilder();

        // Create sample participants
        Participant p1 = new Participant("P001","Lia","l@uni.edu","FIFA",5,"Supporter",56.0,"Thinker");
        Participant p2 = new Participant("P002","Ali","a@uni.edu","Valorant",7,"Leader",95.0,"Leader");
        Participant p3 = new Participant("P003","Sara","s@uni.edu","FIFA",6,"Strategist",80.0,"Balanced");
        Participant p4 = new Participant("P004","John","j@uni.edu","Basketball",8,"Defender",70.0,"Leader");

        List<Participant> participants = new ArrayList<>(Arrays.asList(p1,p2,p3,p4));

        // Test 1: Sorting
        List<Participant> sorted = builder.sortParticipants(participants);
        System.out.println("SortParticipants | Expected first: Ali (Leader, 95.0) | Actual first: " + sorted.get(0).getName());

        // Test 2: Form Teams
        TeamFormationResult result = builder.formTeams(participants, 2, 2);
        System.out.println("FormTeams | Expected Teams: 2 | Actual Teams: " + result.teams.size());
        System.out.println("FormTeams | Expected Unassigned: 0 | Actual Unassigned: " + result.unassignedParticipants.size());

        // Test 3: Leader Allocation
        long leadersInTeam1 = result.teams.get(0).getPersonalityCount("Leader");
        long leadersInTeam2 = result.teams.get(1).getPersonalityCount("Leader");
        System.out.println("Leader Allocation | Expected: 1 per team | Actual: Team1=" + leadersInTeam1 + ", Team2=" + leadersInTeam2);

        // Test 4: Game Cap
        List<Participant> fifaPlayers = Arrays.asList(
                new Participant("P005","Mary","m@uni.edu","FIFA",4,"Supporter",50.0,"Balanced"),
                new Participant("P006","Tom","t@uni.edu","FIFA",6,"Strategist",60.0,"Thinker"),
                new Participant("P007","Zara","z@uni.edu","FIFA",7,"Leader",90.0,"Leader")
        );
        TeamFormationResult capResult = builder.formTeams(fifaPlayers, 3, 2);
        System.out.println("GameCap | Expected Unassigned: 1 | Actual Unassigned: " + capResult.unassignedParticipants.size());
    }




}
