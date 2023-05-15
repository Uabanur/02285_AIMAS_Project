package dtu.aimas;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.helpers.LevelSolver;
import dtu.aimas.search.solvers.agent.IterativeBoxSolver;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IterativeBoxSolverTest {

    @Before
    public void setup() {
        IO.logLevel = LogLevel.Information;
    }

    @Test
    public void Test_SAsimple4_IterativeBoxSolver(){
        LevelSolver.testMap("SAsimple4", new IterativeBoxSolver());
    }

    @Test
    public void Test_SAsoko3_04(){
        LevelSolver.testMap("SAsoko3_04", new IterativeBoxSolver());
    }

    @Ignore
    @Test
    public void Test_Saigon03() {
        LevelSolver.testMap("SAtowersOfSaigon03", new IterativeBoxSolver());
    }
}
