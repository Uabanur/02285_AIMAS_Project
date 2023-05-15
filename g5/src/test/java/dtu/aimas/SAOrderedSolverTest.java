package dtu.aimas;

import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.helpers.LevelSolver;
import dtu.aimas.search.problems.AgentBoxAssignationSplitter;
import dtu.aimas.search.problems.AgentProblemSplitter;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.solvers.blackboard.BlackboardSolver;
import dtu.aimas.search.solvers.graphsearch.*;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.heuristics.GoalCount;
import dtu.aimas.search.solvers.heuristics.MAAdmissibleCost;
import dtu.aimas.search.solvers.heuristics.SingleGoalDistanceCost;
import dtu.aimas.search.solvers.safeinterval.SafePathSolver;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.SAOrderedSolver;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.conflictbasedsearch.ConflictBasedSearch;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class SAOrderedSolverTest {

    
    @Test
    public void MAPF03B_SAOrdered_SafePath_AStar() {
        var solver = new SAOrderedSolver(new AStar(new DistanceSumCost()), new AgentBoxAssignationSplitter());
        LevelSolver.testMap("MAPF03B", solver);
    }

    @Test
    public void MAPF03C_SAOrdered_SafePath_AStar() {
        var solver = new SafePathSolver(
                new SAOrderedSolver(new AStar(new DistanceSumCost())),
                new AgentProblemSplitter(),
                1000
        );
        LevelSolver.testMap("MAPF03C", solver);
    }

    @Ignore //is instant locally, GH runs out of memory...
    @Test
    public void TestSAtowersOfSaigon03_SAOrdered_AStar() {
        LevelSolver.testMap("SAtowersOfSaigon04", new SAOrderedSolver(new AStar(new SingleGoalDistanceCost())));
    }

    @Ignore //is instant locally, GH runs out of memory...
    @Test
    public void TestSAtowersOfSaigon03_SAOrdered_SafePath_AStar() {
        var solver = new SAOrderedSolver(new AStar(new DistanceSumCost()), new AgentBoxAssignationSplitter());
        LevelSolver.testMap("mishmash_r4", solver);
    }
}
