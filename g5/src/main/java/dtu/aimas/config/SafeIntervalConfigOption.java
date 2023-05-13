package dtu.aimas.config;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.UnknownArguments;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.problems.ProblemSplitter;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.agent.IterativeBoxSolver;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.safeinterval.SafeIntervalSolver;

import java.util.List;

public class SafeIntervalConfigOption extends ConfigOption{
    public static final String OptionName = "si";
    public String getOptionName() {return OptionName;}

    private Solver subSolver = new AStar(new DistanceSumCost());
    private ProblemSplitter splitter = new ColorProblemSplitter();
    public void apply(Configuration conf) {
        conf.setSolver(new SafeIntervalSolver(subSolver, splitter));
    }

    public Result<ConfigOption> bindInner(List<String> tokens) {
        IO.debug("tokens: " + tokens);
        for(var token: tokens){
            switch(token){
                case "sub:ibs" ->{
                    subSolver = new IterativeBoxSolver();
                }
                default-> {
                    return Result.error(new UnknownArguments(tokens));
                }
            }
        }

        return Result.ok(this);
    }
}
