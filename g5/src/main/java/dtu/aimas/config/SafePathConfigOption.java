package dtu.aimas.config;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.UnknownArguments;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.problems.ProblemSplitter;
import dtu.aimas.search.problems.RegionProblemSplitter;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.safeinterval.SafePathSolver;

import java.util.List;

public class SafePathConfigOption extends ConfigOption{
    public static final String OptionName = "safepath";
    public String getOptionName() {return OptionName;}

    private Solver subSolver = new AStar(new DistanceSumCost());
    private ProblemSplitter splitter = new ColorProblemSplitter();
    public void apply(Configuration conf) {
        conf.setSolver(new SafePathSolver(subSolver, splitter));
    }

    public Result<ConfigOption> bindInner(List<String> tokens) {
        IO.debug("tokens: " + tokens);
        for(var token: tokens){
            switch(token){
                case "split:region" -> splitter = new RegionProblemSplitter();
                case "split:color"-> splitter = new ColorProblemSplitter();
                default -> {
                    return Result.error(new UnknownArguments(tokens));
                }
            }
        }

        return Result.ok(this);
    }
}
