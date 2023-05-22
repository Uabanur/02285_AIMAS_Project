package dtu.aimas.config;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.search.problems.AgentBoxAssignationSplitter;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.problems.RegionProblemSplitter;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.graphsearch.BFS;
import dtu.aimas.search.solvers.heuristics.GuidedDistanceSumCost;
import dtu.aimas.search.solvers.safeinterval.SafePathSolver;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter
public class Configuration {
    public Configuration(){
        solver = new SafePathSolver(
                    new SafePathSolver(
                            new SafePathSolver(
                                    new AStar(new GuidedDistanceSumCost()),
                                    new AgentBoxAssignationSplitter(),
                                    100),
                            new ColorProblemSplitter(),
                            10),
                    new RegionProblemSplitter());

        groupName = "Group5";
        logLevel = LogLevel.Information;
        debugServerMessages = false;
    }

    @NonNull
    private String groupName;

    @NonNull
    private Solver solver;

    private LogLevel logLevel;
    private Boolean debugServerMessages;

    public void configureIO(){
        IO.logLevel = logLevel;
        IO.debugServerMessages = debugServerMessages;
    }

    @Override
    public String toString() {
        return String.format("Configuration{Solver=%s; GroupName=%s; LogLevel=%s; DebugServerMessages=%b}", 
            solver.getClass().getSimpleName(),
            groupName,
            logLevel.name(),
            debugServerMessages
        );
    }
}
