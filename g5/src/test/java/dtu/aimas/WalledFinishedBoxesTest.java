package dtu.aimas;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.search.solvers.agent.IterativeBoxSolver;
import dtu.aimas.search.solvers.agent.WalledFinishedBoxes;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class WalledFinishedBoxesTest extends  LevelSolvingTest{

    @Before
    public void setup() {
        IO.logLevel = LogLevel.Debug;
    }

    @Test
    public void Test_SAsimple4_WalledFinishedBoxes(){
        TestMap("SAsimple4", new WalledFinishedBoxes());
    }

    @Test
    public void Test_SAsoko3_08(){
        TestMap("SAsoko3_08", new WalledFinishedBoxes());
    }

    @Ignore
    @Test
    public void Test_Saigon03() {
        TestMap("SAtowersOfSaigon03", new WalledFinishedBoxes());
    }
}
