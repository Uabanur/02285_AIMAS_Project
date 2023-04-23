package dtu.aimas.config;

import dtu.aimas.common.Result;
import dtu.aimas.errors.UnknownArguments;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.solvers.blackboard.BlackboardSolver;
import dtu.aimas.search.solvers.graphsearch.AStarMinLength;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;

import java.util.List;

public class BlackboardConfigOption extends ConfigOption{
    public static final String OptionName = "bb";
    public String getOptionName() {return OptionName;}

    public void apply(Configuration conf) {
        conf.setSolver(new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new DistanceSumCost()));
    }

    public Result<ConfigOption> bindInner(List<String> tokens) {
        if(!tokens.isEmpty())
            return Result.error(
                    new UnknownArguments(tokens));

        return Result.ok(this);
    }
}
