package dtu.aimas.config;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.UnknownArguments;
import dtu.aimas.search.problems.AgentBoxAssignationSplitter;
import dtu.aimas.search.problems.AgentProblemSplitter;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.problems.RegionProblemSplitter;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.heuristics.GuidedDistanceSumCost;
import dtu.aimas.search.solvers.safeinterval.SafePathSolver;

import java.util.List;

public class SafePathConfigOption extends ConfigOption{
    public static final String OptionName = "safepath";
    public String getOptionName() {return OptionName;}

//    private Solver subSolver = new AStar(new DistanceSumCost());
//    private ProblemSplitter splitter = new ColorProblemSplitter();
    private Solver solver = new SafePathSolver();
    public void apply(Configuration conf) {
        conf.setSolver(solver);
    }

    public Result<ConfigOption> bindInner(List<String> tokens) {
        IO.debug("tokens: " + tokens);
        for(var token: tokens){
            switch(token){
                case "attempt1:region:color:agent:guided" -> {
                    solver = new SafePathSolver(
                            new SafePathSolver(
                                    new SafePathSolver(
                                            new AStar(new GuidedDistanceSumCost()),
                                            new AgentProblemSplitter(),
                                            10
                                    ),
                                    new ColorProblemSplitter(),
                                    10
                            ),
                                new RegionProblemSplitter()
                        );
                }
                case "attempt2:region:color:agentassign:guided" -> {
                    solver = new SafePathSolver(
                            new SafePathSolver(
                                new SafePathSolver(
                                        new AStar(new GuidedDistanceSumCost()),
                                        new AgentBoxAssignationSplitter(),
                                        10
                                ),
                                new ColorProblemSplitter(),
                                10
                        ),
                        new RegionProblemSplitter()
                );
                }
                default -> {
                    return Result.error(new UnknownArguments(tokens));
                }
            }
        }

        return Result.ok(this);
    }
}
