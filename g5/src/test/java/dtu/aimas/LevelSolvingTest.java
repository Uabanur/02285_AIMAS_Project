package dtu.aimas;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.helpers.LevelSolver;
import dtu.aimas.helpers.SolveLevelTask;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.*;
import dtu.aimas.search.solvers.heuristics.*;
import dtu.aimas.search.solvers.subsolvers.AgentSolver;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;


public class LevelSolvingTest {
    private void TestMap(String levelName, Solver solver)
    {
        var solution = LevelSolver.solve(levelName, parser, solver, logOutputToFile);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    private void TestMap(String levelName, Solver solver, long timeout, TimeUnit timeUnit) {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Result<Solution>> future = executor.submit(new SolveLevelTask(levelName, parser, solver, logOutputToFile));

            try {
                var solution = future.get(timeout, timeUnit);
                Assert.assertTrue(solution.toString(), solution.isOk());
            } catch (TimeoutException e) {
                future.cancel(true);
                IO.logException(e);
                Assert.fail("Timeout exceeded");
            } finally {
                executor.shutdownNow();
            }

         } catch (Exception e) {
                IO.logException(e);
                Assert.fail("Unexpected error occurred: " + e.getMessage());
        }
    }

    static final boolean logOutputToFile = false;
    static final LevelParser parser = CourseLevelParser.Instance;

    @Test
    public void TestMAPF00_BFS_TimeLimit_500ms() {
        TestMap("MAPF00", new BFS(), 500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void TestMAPF00_DFS_TimeLimit_500ms() {
        TestMap("MAPF00", new DFS(), 500, TimeUnit.MILLISECONDS);
    }

    @Test
    public void TestMAPF01_BFS_TimeLimit_1Second() {
        TestMap("MAPF01", new BFS(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void TestMAPF01_DFS_TimeLimit_1Second() {
        TestMap("MAPF01", new DFS(), 1, TimeUnit.SECONDS);
    }
    @Test
    public void TestMAPF00_AStar_GoalCount() {
        TestMap("MAPF00", new AStar(new GoalCount()));
    }

    @Test
    public void TestMAPF00_AStar_DistanceCost() {
        TestMap("MAPF00", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAPF00_Greedy_GoalCount() {
        TestMap("MAPF00", new Greedy(new GoalCount()));
    }

    @Test
    public void TestMAPF01_BFS() {
        TestMap("MAPF01", new BFS(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void TestMAPF01_DFS() {
        TestMap("MAPF01", new DFS(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void TestMAPF01_AStar_GoalCount() {
        TestMap("MAPF01", new AStar(new GoalCount()));
    }

    @Test
    public void TestMAPF01_AStar_MAAdmissibleCost() {
        TestMap("MAPF01", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAPF01_Greedy_GoalCount() {
        TestMap("MAPF01", new Greedy(new GoalCount()));
    }

    @Test
    public void TestSAD1_BFS() {
        TestMap("SAD1", new BFS());
    }

    @Test
    public void TestMAPF02_AStar_DistanceCost() {
        TestMap("MAPF02", new AStar(new DistanceSumCost()));
    }

    @Test
    public void TestMAPF02_AStar_MAAdmissibleCost() {
        TestMap("MAPF02", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAsimple4_AStar_DistanceCost() {
        TestMap("MAsimple4", new AStar(new DistanceSumCost()));
    }

    @Test
    public void TestMAsimple4_AStar_MAAdmissibleCost() {
        TestMap("MAsimple4", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAsimple4_AStar_GoalCount() {
        TestMap("MAsimple4", new AStar(new GoalCount()));
    }

    @Test
    public void TestMAsimple5_AStar_DistanceCost() {
        TestMap("MAsimple5", new AStar(new DistanceSumCost()));
    }

    @Test
    public void TestMAsimple5_AStar_MAAdmissibleCost() {
        TestMap("MAsimple5", new AStar(new MAAdmissibleCost()));
    }

    @Test
    public void TestMAsimple5_AStar_GoalCount() {
        TestMap("MAsimple5", new AStar(new GoalCount()));
    }

    @Test
    public void TestSAD3_AgentSolver(){
        TestMap("SAD3", new AgentSolver(AStarMinLength::new));
    }
}
