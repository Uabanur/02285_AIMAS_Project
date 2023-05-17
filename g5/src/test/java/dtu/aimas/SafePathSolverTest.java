package dtu.aimas;

import dtu.aimas.common.Position;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.helpers.LevelSolver;
import dtu.aimas.search.problems.AgentBoxAssignationSplitter;
import dtu.aimas.search.problems.AgentProblemSplitter;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.problems.RegionProblemSplitter;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;
import dtu.aimas.search.solvers.heuristics.GuidedDistanceSumCost;
import dtu.aimas.search.solvers.safeinterval.ReservedCell;
import dtu.aimas.search.solvers.safeinterval.SafePathSolver;
import dtu.aimas.search.solvers.safeinterval.SafeProblem;
import dtu.aimas.search.solvers.safeinterval.TimeInterval;
import org.junit.*;

import java.util.List;

import static dtu.aimas.helpers.LevelHelper.getProblem;

public class SafePathSolverTest {
    private Result<Solution> solution;
    private long startTimeMs = 0;

    @Before
    public void setup(){
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
    public void Empty(){
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

        var problem = getProblem(level, "red:0");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
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

        var problem = getProblem(level, "red:0,A");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
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
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
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
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void VertexAndEdgeConflicts(){
        var level = """
                    #initial
                    +++++
                    +1 0+
                    ++ ++
                    ++ ++
                    +++++
                    #goal
                    +++++
                    +0 1+
                    ++ ++
                    ++ ++
                    +++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0", "blue: 1");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void FollowMoveConflict(){
        var level = """
                    #initial
                    ++++++
                    +10  +
                    ++++++
                    #goal
                    ++++++
                    +  10+
                    ++++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0", "blue: 1");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void FollowPushConflict(){
        var level = """
                    #initial
                    ++++++
                    +0A1 +
                    ++++++
                    #goal
                    ++++++
                    + 0A1+
                    ++++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0, A", "blue: 1");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void FollowPullConflict(){
        var level = """
                    #initial
                    ++++++
                    +0A1 +
                    ++++++
                    #goal
                    ++++++
                    + 0A1+
                    ++++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0", "blue: 1, A");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void ThreeAgents_Crossing(){
        var level = """
                    #initial
                    +++++++
                    ++0+ ++
                    +2    +
                    ++1+ ++
                    +++++++
                    #goal
                    +++++++
                    ++ +0++
                    +    2+
                    ++ +1++
                    +++++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0","blue: 1","green: 2");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void TwoColors_BottleneckBoxConflicts(){
        var level = """
                    #initial
                    ++++++++
                    +0A+   +
                    ++   +++
                    +1B+   +
                    ++++++++
                    #goal
                    ++++++++
                    +  +  A+
                    ++   +++
                    +  +  B+
                    ++++++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0, A", "blue: 1, B");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Ignore
    @Test
    public void ThreeColors_BottleneckBoxConflicts(){
        var level = """
                    #initial
                    ++++++++++
                    +++0A+   +
                    ++++ + +++
                    +2C      +
                    ++++ + +++
                    +++1B+   +
                    ++++++++++
                    #goal
                    ++++++++++
                    +++  +  A+
                    ++++ + +++
                    +       C+
                    ++++ + +++
                    +++  +  B+
                    ++++++++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0, A", "blue: 1, B", "cyan: 2, C");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test
    public void TwoColors_FewBoxConflicts(){
        var level = """
                    #initial
                    ++++++++
                    +0A    +
                    +1B    +
                    ++++++++
                    #goal
                    ++++++++
                    +     B+
                    +     A+
                    ++++++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0, A", "blue: 1, B");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Ignore
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
        var problem = getProblem(level, "red: 0,A", "blue: 1,B", "green: 2,C", "cyan: 3");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
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
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.getErrorMessageOrEmpty(), solution.isOk());
    }

    @Ignore
    @Test
    public void RowsOfAgentsAndBoxes(){
        var level = """
                #initial
                +++++++++++
                +0A       +
                +1B       +
                +2C       +
                +3D       +
                +++++++++++
                #goal
                +++++++++++
                +        A+
                +        C+
                +        D+
                +        B+
                +++++++++++
                #end
                """;

        var problem = getProblem(level,
            "red: 0, A",
            "blue: 1, B",
            "green: 2, C",
            "cyan: 3, D"
        );

        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                100
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.getErrorMessageOrEmpty(), solution.isOk());
    }

    @Test
    public void BlockingFinish(){
        var level = """
                #initial
                +++++++++++
                +0        +
                +++++++1+++
                +++++++++++
                #goal
                +++++++++++
                +      1 0+
                +++++++ +++
                +++++++++++
                #end
                """;
        var problem = getProblem(level, "red: 0", "blue: 1");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
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
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new ColorProblemSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Ignore
    @Test
    public void AgentBoxSplitter(){
        var level = """
                #initial
                +++++++++
                +0 D+A 1+
                +ABC+BDC+
                +   +   +
                +   +   +
                +++++++++
                #goal
                +++++++++
                +   +   +
                +   +   +
                +  D+A  +
                +ABC+BDC+
                +++++++++
                #end
                """;

        var problem = getProblem(level, "red: 0,1,A,B,C,D");
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new AgentBoxAssignationSplitter(),
                true
        );
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Ignore
    @Test
    public void Test_MishMash_R2(){

        var solver =
                new SafePathSolver(

                        new SafePathSolver(
                                new AStar(new DistanceSumCost()),
                                new AgentProblemSplitter(),
                                50
                ),
                new RegionProblemSplitter()
        );

        IO.logLevel = LogLevel.Debug;
        LevelSolver.testMap("mishmash_r2", solver);
    }

    @Test
    public void Test_MishMash_Region_With_AStar(){
        var solver = new SafePathSolver(
                new AStar(new DistanceSumCost()),
                new RegionProblemSplitter()
        );

        IO.logLevel = LogLevel.Debug;
        var solution = LevelSolver.solve("mishmash", IO.CompLevelDir, solver);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Ignore
    @Test
    public void Test_MishMash_RegionSplit(){
        var solver = new SafePathSolver(
            new SafePathSolver(
                new SafePathSolver(
                    new AStar(new DistanceSumCost()),
//                    new Focal(new DistanceSumCost(), 2),
                    new AgentProblemSplitter(),
                    50
                ),
                new ColorProblemSplitter(),
                    10
            ),
            new RegionProblemSplitter()
        );

        IO.logLevel = LogLevel.Debug;
        var solution = LevelSolver.solve("mishmash", IO.LevelDir, solver);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Ignore
    @Test
    public void Test_comp_sixty(){
        var solver = new SafePathSolver(
                new SafePathSolver(
                        new SafePathSolver(
                                new AStar(new GuidedDistanceSumCost()),
//                    new Focal(new DistanceSumCost(), 2),
//                                new WalledFinishedBoxes(),
                                new AgentProblemSplitter(),
                                10
                        ),
                        new ColorProblemSplitter(),
                        10
                ),
                new RegionProblemSplitter()
        );

        IO.logLevel = LogLevel.Debug;
        for(var level: List.of("colada", "sixty", "mishmash")){
            var solution = LevelSolver.solve(level, IO.CompLevelDir, solver);
            Assert.assertTrue(solution.toString(), solution.isOk());
        }
    }

    @Test
    public void Test_Nested_SafeProblem(){

        var level = """
                #initial
                +++++
                +0  +
                +A  +
                +   +
                +++++
                #goal
                +++++
                +   +
                +   +
                +A  +
                +++++
                #end
                """;

        var problem = getProblem(level, "red: 0, A");


        var end = Integer.MAX_VALUE;
        var restrictions = List.of(
                new ReservedCell(
                        new Position(3, 1),
                        new TimeInterval(0, 2)
                )
                ,new ReservedCell(
                        new Position(2, 1),
                        new TimeInterval(1, end)
                )
        );

        var safeProblem = SafeProblem.from(problem, restrictions).orElseThrow();

        IO.logLevel = LogLevel.Debug;
        solution = new AStar(new DistanceSumCost()).solve(safeProblem);
        Assert.assertTrue(solution.isOk());
    }
}
