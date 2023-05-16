package dtu.aimas.helpers;

import dtu.aimas.common.Result;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public class SolveLevelTask implements Callable<Result<Solution>> {
    private final String levelName;
    private final Path directory;
    private final LevelParser parser;
    private final Solver solver;
    private final boolean logOutputToFile;

    public SolveLevelTask(String levelName, Path directory, LevelParser parser, Solver solver, boolean logOutputToFile) {
        this.levelName = levelName;
        this.directory = directory;
        this.parser = parser;
        this.solver = solver;
        this.logOutputToFile = logOutputToFile;
    }

    @Override
    public Result<Solution> call() {
        return LevelSolver.solve(levelName, directory, parser, solver, logOutputToFile);
    }
}
