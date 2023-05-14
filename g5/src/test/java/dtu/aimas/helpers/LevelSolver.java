package dtu.aimas.helpers;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import org.junit.Assert;

import java.util.concurrent.*;

public class LevelSolver {
    public static Result<Solution> solve(String levelName, LevelParser parser, Solver solver, boolean logOutputToFile) {
        if (logOutputToFile) IO.logOutputToFile(solver.getClass().getSimpleName() + "_" + levelName);
        var solution = FileHelper.loadLevel(levelName, parser).flatMap(solver::solve);
        solution.ifOk(s -> {
            IO.debug("Solution:");
            s.serializeSteps().forEach(IO::debug);
        });
        return solution;
    }

    public static Result<Solution> solve(String levelName, Solver solver) {
        return solve(levelName, CourseLevelParser.Instance, solver, false);
    }

    public static void testMap(String levelName, Solver solver){
        var solution = solve(levelName, solver);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    public static void testMap(String levelName, Solver solver, long timeout, TimeUnit timeUnit) {
        testMap(levelName, solver, timeout, timeUnit, CourseLevelParser.Instance, false);
    }

    public static void testMap(String levelName, Solver solver, long timeout, TimeUnit timeUnit, LevelParser parser, boolean logOutputToFile) {
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
}
