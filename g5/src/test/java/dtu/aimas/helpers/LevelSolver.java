package dtu.aimas.helpers;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;

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
}
