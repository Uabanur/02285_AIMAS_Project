package dtu.aimas.config;

import java.util.List;

import dtu.aimas.common.Result;
import dtu.aimas.errors.UnknownArguments;
import dtu.aimas.search.solvers.SAOrderedSolver;
import dtu.aimas.search.solvers.conflictbasedsearch.ConflictBasedSearch;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.graphsearch.AStarMinLength;
import dtu.aimas.search.solvers.graphsearch.BFS;
import dtu.aimas.search.solvers.graphsearch.Greedy;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.heuristics.SingleGoalDistanceCost;
import dtu.aimas.search.solvers.heuristics.MAAdmissibleCost;

public class CBSConfigOption extends ConfigOption {
    public static final String OptionName = "cbs";
    public String getOptionName() {
        return OptionName;
    }

    public void apply(Configuration conf) {
        conf.setSolver(new ConflictBasedSearch(new SAOrderedSolver(new AStar(new SingleGoalDistanceCost()))));
    }
    
    public Result<ConfigOption> bindInner(List<String> tokens) {
        if(!tokens.isEmpty()) 
            return Result.error(
                new UnknownArguments(tokens));

        return Result.ok(this);
    }
}