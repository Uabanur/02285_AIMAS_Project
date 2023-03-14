package dtu.aimas.parsers;

import dtu.aimas.common.Result;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.BFS;

public class ArgumentParser {
    public static Result<Solver> parseSolverFromArguments(String[] args){
        return Result.ok(new BFS());
    }
}
