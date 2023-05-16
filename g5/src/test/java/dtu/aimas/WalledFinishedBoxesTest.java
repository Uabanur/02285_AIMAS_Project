package dtu.aimas;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.helpers.LevelSolver;
import dtu.aimas.search.solvers.agent.WalledFinishedBoxes;
import dtu.aimas.search.solvers.graphsearch.Focal;
import dtu.aimas.search.solvers.graphsearch.Greedy;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WalledFinishedBoxesTest {

    @Before
    public void setup() {
        IO.logLevel = LogLevel.Information;
    }

    @Test
    public void Test_SAsimple4_WalledFinishedBoxes(){
        LevelSolver.testMap("SAsimple4", new WalledFinishedBoxes());
    }

    @Test
    public void Test_SAsoko3_08(){
        LevelSolver.testMap("SAsoko3_08", new WalledFinishedBoxes());
    }

    @Ignore
    @Test
    public void Test_Saigon03() {
        LevelSolver.testMap("SAtowersOfSaigon03", new WalledFinishedBoxes(new Greedy(new DistanceSumCost())));
    }

     
    @Test
    public void AgentGoalAfterBoxGoals(){
        LevelSolver.testMap("AgentGoalAfterBoxGoals", new WalledFinishedBoxes());
    }

    // caution - not passing
    // case to discuss
    @Ignore 
    @Test
    public void AgentBlockingHimself(){
        LevelSolver.testMap("AgentBlockingHimself", new WalledFinishedBoxes());
    }

}
