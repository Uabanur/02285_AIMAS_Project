package dtu.aimas.helpers;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import org.junit.Assert;

import java.nio.file.Path;
import java.util.concurrent.*;

public class LevelSolver {
    public static Result<Solution> solve(String levelName, Path directory, LevelParser parser, Solver solver, boolean logOutputToFile) {
        if (logOutputToFile) IO.logOutputToFile(solver.getClass().getSimpleName() + "_" + levelName);
        IO.debug("Solving level: %s", levelName);
        var solution = FileHelper.loadLevel(levelName, directory, parser)
                .map(p -> {IO.debug("Problem:\n" + p); return p;})
                .flatMap(solver::solve);
        solution.ifOk(s -> {
            IO.debug("Solution:");
            s.serializeSteps().forEach(IO::debug);
        });
        return solution;
    }

    public static Result<Solution> solve(String levelName, Path directory, Solver solver) {
        return solve(levelName, directory, CourseLevelParser.Instance, solver, false);
    }

    public static void testMap(String levelName, Path directory, Solver solver){
        var solution = solve(levelName, directory ,solver);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    public static void testMap(String levelName, Solver solver){
        testMap(levelName, IO.LevelDir, solver);
    }

    public static void testMap(String levelName, Solver solver, long timeout, TimeUnit timeUnit) {
        testMap(levelName, IO.LevelDir, solver, timeout, timeUnit);
    }

    public static void testMap(String levelName, Path directory, Solver solver, long timeout, TimeUnit timeUnit) {
        var solution = solve(levelName, directory, solver, timeout, timeUnit, CourseLevelParser.Instance, false);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    public static Result<Solution> solve(String levelName, Path directory, Solver solver, long timeout, TimeUnit timeUnit) {
        return solve(levelName, directory, solver, timeout, timeUnit, false);
    }

    public static Result<Solution> solve(String levelName, Path directory, Solver solver, long timeout, TimeUnit timeUnit, boolean logOutputToFile) {
        return solve(levelName, directory, solver, timeout, timeUnit, CourseLevelParser.Instance, logOutputToFile);
    }

    public static Result<Solution> solve(String levelName, Path directory, Solver solver, long timeout, TimeUnit timeUnit, LevelParser parser, boolean logOutputToFile) {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Result<Solution>> future = executor.submit(new SolveLevelTask(levelName, directory, parser, solver, logOutputToFile));

            try {
                return future.get(timeout, timeUnit);
            } catch (TimeoutException e) {
                future.cancel(true);
                IO.error("Time limit exceeded " + timeout + " " + timeUnit);
                return Result.error(e);
            } finally {
                executor.shutdownNow();
            }

        } catch (Exception e) {
            return Result.error(e);
        }
    }
}
