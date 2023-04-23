package dtu.aimas;

import dtu.aimas.common.Color;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.search.problems.AgentProblemSplitter;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.blackboard.BlackboardSolver;
import dtu.aimas.search.solvers.graphsearch.AStarMinLength;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.heuristics.GoalCount;
import dtu.aimas.search.solvers.subsolvers.AgentSolver;
import org.junit.*;

import java.util.stream.IntStream;

import static dtu.aimas.helpers.LevelHelper.getProblem;

public class BlackboardSolverTest {
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
    public void EmptyProblem() {
        var level = """
                    #initial
                    +++
                    + +
                    +++
                    #goal
                    +++
                    + +
                    +++
                    #end
                    """;

        var problem = getProblem(level, "red: 0");
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test 
    public void SingleAgent1Box() {
        var level = """
                    #initial
                    +++++
                    +0A +
                    +++++
                    #goal
                    +++++
                    +  A+
                    +++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0, A");
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void TwoAgentsNoConflict(){
        var level = """
                    #initial
                    +++++
                    +0A +
                    +++++
                    +1B +
                    +++++
                    #goal
                    +++++
                    +  A+
                    +++++
                    +  B+
                    +++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0, A", "blue: 1, B");
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void TwoAgents_Crossing(){
        var level = """
                    #initial
                    +++++
                    ++0++
                    +1  +
                    ++ ++
                    +++++
                    #goal
                    +++++
                    ++ ++
                    +  1+
                    ++0++
                    +++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0", "blue: 1");
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new GoalCount());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void MultipleAgentsDifferentColors(){
        var level = """
                #initial
                ++++++++++
                +01A    3+
                +  B     +
                +        +
                +   2C   +
                ++++++++++
                #goal
                ++++++++++
                +       C+
                +        +
                +       A+
                +B      3+
                ++++++++++
                #end
                """;
        var problem = getProblem(level, "red: 0,A", "blue: 1,B", "green: 2,C");
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new DistanceSumCost());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void ThreeAgentsCrossing(){
        var level = """
                #initial
                +++++++
                +01   +
                +++ +++
                +  2  +
                +++++++
                #goal
                +++++++
                +    2+
                +++ +++
                +   01+
                +++++++
                #end
                """;
        var problem = getProblem(level, "red: 0,1,2,3,4");
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new DistanceSumCost());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.getErrorMessageOrEmpty(), solution.isOk());
    }

    @Test
    public void RowsOfAgentsAndBoxes(){
        var level = """
                #initial
                +++++++++++
                +0A       +
                +1B       +
                +2C       +
                +3D       +
                +4E       +
                +++++++++++
                #goal
                +++++++++++
                +        E+
                +        A+
                +        B+
                +        C+
                +        D+
                +++++++++++
                #end
                """;
        var colors = IntStream.range(0, 5).mapToObj(i ->
                (Color.values()[i]).name() + ": " + i + "," + (char)('A'+i)
        ).toArray(String[]::new);

        var problem = getProblem(level, colors);
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new DistanceSumCost());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.getErrorMessageOrEmpty(), solution.isOk());
    }

    @Test
    public void BlockingFinish(){
        var level = """
                #initial
                +++++++
                +0    +
                +++1+++
                +++++++
                #goal
                +++++++
                +  1 0+
                +++ +++
                +++++++
                #end
                """;
        var problem = getProblem(level, "red: 0, 1");
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new DistanceSumCost());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.getErrorMessageOrEmpty(), solution.isOk());
    }

    @Test
    public void SameColorAgentsWithBoxes(){
        var level = """
                #initial
                +++++
                +01 +
                +AB +
                +   +
                +++++
                #goal
                +++++
                +   +
                +   +
                +AB +
                +++++
                #end
                """;

        var problem = getProblem(level, "red: 0,1,A,B");
        var solver = new BlackboardSolver(new ColorProblemSplitter(), AStarMinLength::new, new DistanceSumCost());
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void AgentSolver_With_Conflict(){
        var level = """
                #initial
                +++++
                +0 1+
                +A B+
                +   +
                +++++
                #goal
                +++++
                +   +
                +   +
                +B A+
                +++++
                #end
                """;

        var problem = getProblem(level, "red: 0,A", "blue: 1,B");

        var solver = new BlackboardSolver(
                new AgentProblemSplitter(),
                c -> new AgentSolver(AStarMinLength::new, c),
                new DistanceSumCost()
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Ignore
    @Test
    public void AgentSolver_With_More_Conflicts(){
        var level = """
                #initial
                +++++++
                +01   +
                +AB   +
                +     +
                +++++++
                #goal
                +++++++
                +     +
                +     +
                +BA   +
                +++++++
                #end
                """;

        var colors = IntStream.range(0, 5).mapToObj(i ->
                (Color.values()[i]).name() + ": " + i + "," + (char)('A'+i)
        ).toArray(String[]::new);

        var problem = getProblem(level, colors);

        var solver = new BlackboardSolver(
                new AgentProblemSplitter(),
//                AgentSolver::new,
                AStarMinLength::new,
//                DefaultCost.instance
                DistanceSumCost.instance
        );

        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }
}
