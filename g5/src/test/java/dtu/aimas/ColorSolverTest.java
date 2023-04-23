package dtu.aimas;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.search.problems.AgentProblemSplitter;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.SolverMinLength;
import dtu.aimas.search.solvers.blackboard.BlackboardSolver;
import dtu.aimas.search.solvers.graphsearch.AStarMinLength;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.DefaultCost;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.heuristics.GoalCount;
import dtu.aimas.search.solvers.subsolvers.AgentSolver;
import dtu.aimas.search.solvers.subsolvers.ColorSolver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

import static dtu.aimas.helpers.LevelHelper.getProblem;

public class ColorSolverTest {
    private Result<Solution> solution;
    private long startTimeMs = 0;

    @Before
    public void setup(){
        IO.logLevel = LogLevel.Debug;
        startTimeMs = System.currentTimeMillis();
        solution = Result.error(new SolutionNotFound());
    }

    @After
    public void after(){
        IO.debug("Test time: %d ms", System.currentTimeMillis() - startTimeMs);
        solution.ifOk(s -> {
            IO.debug("Solution of size %d found:\n", s.size());
            s.serializeSteps().forEach(IO::debug);
        });
    }

    @Test
    public void EmptyTest(){
        var level = """
                    #initial
                    +++
                    +0+
                    +++
                    #goal
                    +++
                    + +
                    +++
                    #end
                    """;

        var problem = getProblem(level, "red: 0");
        var solver = new ColorSolver(AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void TwoAgentsWithBoxes_With_AStarMinLength(){
        var level = """
                    #initial
                    ++++
                    +01+
                    +AB+
                    +  +
                    ++++
                    #goal
                    ++++
                    +  +
                    +  +
                    +AB+
                    ++++
                    #end
                    """;

        var problem = getProblem(level, "red: 0,1,A,B");
        var solver = new ColorSolver(AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void TwoAgentsWithBoxes_With_BlackBoard(){
        var level = """
                    #initial
                    ++++
                    +01+
                    +AB+
                    +  +
                    ++++
                    #goal
                    ++++
                    +  +
                    +  +
                    +AB+
                    ++++
                    #end
                    """;

        var problem = getProblem(level, "red: 0,1,A,B");

        Function<Cost, SolverMinLength> blackboard = baseCost -> new BlackboardSolver(
                new AgentProblemSplitter(),
                c -> new AgentSolver(AStarMinLength::new, c),
                baseCost
        );

        var solver = new ColorSolver(
                blackboard,
                new DistanceSumCost()
        );

        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void ManyAgentsAndBoxes_NotOverlapping_WithBlackboard(){
        var level = """
                    #initial
                    +++++++++++++++++++
                    +  0  +  1  +  2  +
                    +AAAAA+BBBBB+CCCCC+
                    +     +     +     +
                    +     +     +     +
                    +     +     +     +
                    +     +     +     +
                    +++++++++++++++++++
                    #goal
                    +++++++++++++++++++
                    +     +     +     +
                    +     +     +     +
                    +     +     +     +
                    +     +     +     +
                    +     +     +     +
                    + A A + B B + C C +
                    +++++++++++++++++++
                    #end
                    """;

        var problem = getProblem(level, "red: 0,1,2,A,B,C");

        Function<Cost, SolverMinLength> blackboard = baseCost -> new BlackboardSolver(
                new AgentProblemSplitter(),
                c -> new AgentSolver(AStarMinLength::new, c),
                baseCost
        );

        var solver = new ColorSolver(
                blackboard,
                new DistanceSumCost()
        );

        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void TwoAgentsWithManyBoxes_With_BlackBoard(){
        var level = """
                    #initial
                    ++++++
                    + 01 +
                    +ABCD+
                    +    +
                    +    +
                    ++++++
                    #goal
                    ++++++
                    +    +
                    +    +
                    +    +
                    +ABCD+
                    ++++++
                    #end
                    """;

        var problem = getProblem(level, "red: 0,1,A,B,C,D");

        Function<Cost, SolverMinLength> blackboard = baseCost -> new BlackboardSolver(
                new AgentProblemSplitter(),
                AgentSolver::new,
                baseCost
        );

        var solver = new ColorSolver(
                blackboard,
                DefaultCost.instance
        );

        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }
}
