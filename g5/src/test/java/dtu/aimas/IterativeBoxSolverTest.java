package dtu.aimas;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.search.solvers.agent.IterativeBoxSolver;
import org.junit.Before;
import org.junit.Test;

public class IterativeBoxSolverTest extends  LevelSolvingTest{

    @Before
    public void setup() {
        IO.logLevel = LogLevel.Debug;
    }

    @Test
    public void Test_SAsimple4_IterativeBoxSolver(){
        TestMap("SAsimple4", new IterativeBoxSolver());
    }

    @Test
    public void Test_SAsoko3_08(){
        TestMap("SAsoko3_08", new IterativeBoxSolver());
    }
}
