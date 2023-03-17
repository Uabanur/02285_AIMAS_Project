package dtu.aimas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.*;
import dtu.aimas.search.solvers.heuristics.*;


public class LevelTester {

    class Task implements Callable<Result<Solution>> {
        private String levelName;
        private Solver solver;
        public Task(String levelName, Solver solver){
            this.levelName = levelName;
            this.solver = solver;
        }
        @Override
        public Result<Solution> call() throws Exception {
            return SolveLevel(levelName, solver);
        }
    }

    private Result<Reader> getFileReader(String levelName){
        try {
            var levelFile = new File(IO.LevelDir.toFile(), levelName + ".lvl");
            var buffer = new FileReader(levelFile);
            return Result.ok(buffer);
        } catch (FileNotFoundException e) {
            return Result.error(e);
        }
    }

    private Result<Problem> LoadLevel(String levelName) {
        return getFileReader(levelName).flatMap(lvl -> parser.parse(lvl));
    }

    private Result<Solution> SolveLevel(String levelName, Solver solver) {
        if (logOutputToFile) IO.logOutputToFile(solver.getClass().getSimpleName() + "_" + levelName);
        return LoadLevel(levelName).flatMap(solver::solve);
    }

    @SuppressWarnings("unused")
    private void TestMap(String levelName, Solver solver)
    {
        var solution = SolveLevel(levelName, solver);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @SuppressWarnings("unused")
    private void TestMap(String levelName, Solver solver, long timeout, TimeUnit timeUnit) {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Result<Solution>> future = executor.submit(new Task(levelName, solver));

            try {
                var solution = future.get(timeout, timeUnit);
                Assert.assertTrue(solution.toString(), solution.isOk());
            } catch (TimeoutException e) {
                future.cancel(true);
                IO.logException(e);
                Assert.fail("Timeout exceeded");
            } finally {
                if (executor != null) executor.shutdownNow();
            }

         } catch (Exception e) {
                IO.logException(e);
                Assert.fail("Unexpected error occured: " + e.getMessage());
        }
    }

    static final boolean logOutputToFile = false;
    static final LevelParser parser = CourseLevelParser.Instance;

    @Test
    public void TestMAPF00_BFS() {
        TestMap("MAPF00", new BFS(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void TestMAPF00_DFS() {
        TestMap("MAPF00", new DFS(), 1, TimeUnit.SECONDS);
    }

    @Test
    public void TestMAPF00_AStar_GoalCount() {
        TestMap("MAPF00", new AStar(new GoalCount()));
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
    public void TestMAPF01_Greedy_GoalCount() {
        TestMap("MAPF01", new Greedy(new GoalCount()));
    }

}
