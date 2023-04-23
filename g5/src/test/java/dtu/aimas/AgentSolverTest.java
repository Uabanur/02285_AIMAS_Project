package dtu.aimas;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.graphsearch.AStarMinLength;
import dtu.aimas.search.solvers.subsolvers.AgentSolver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static dtu.aimas.helpers.LevelHelper.getProblem;

public class AgentSolverTest {
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
        var solver = new AgentSolver(AStarMinLength::new);
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void SingleBox(){
        var level = """
                    #initial
                    ++++++
                    +0A  +
                    ++++++
                    #goal
                    ++++++
                    +   A+
                    ++++++
                    #end
                    """;

        var problem = getProblem(level, "red: 0,A");
        var solver = new AgentSolver(AStarMinLength::new);
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void ManyBoxesSingleGoal(){
        var level = """
                    #initial
                    ++++++
                    +0A  +
                    +AA  +
                    +AA  +
                    +AA  +
                    +AA  +
                    +AA  +
                    ++++++
                    #goal
                    ++++++
                    +   A+
                    +    +
                    +    +
                    +    +
                    +    +
                    +    +
                    ++++++
                    #end
                    """;

        var problem = getProblem(level, "red: 0,A");
        var solver = new AgentSolver(AStarMinLength::new);
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void DifferentBoxesWithGoals(){
        var level = """
                    #initial
                    +++++++++++++
                    +0          +
                    +A B C D E F+
                    +           +
                    +++++++++++++
                    #goal
                    +++++++++++++
                    +           +
                    +           +
                    +A B C D E F+
                    +++++++++++++
                    #end
                    """;

        var problem = getProblem(level, "red: 0,A,B,C,D,E,F");
        var solver = new AgentSolver(AStarMinLength::new);
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }
}
