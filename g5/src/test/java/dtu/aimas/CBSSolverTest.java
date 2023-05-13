package dtu.aimas;

import dtu.aimas.common.*;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.conflictbasedsearch.ConflictBasedSearch;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.graphsearch.Greedy;
import dtu.aimas.search.solvers.heuristics.MAAdmissibleCost;
import dtu.aimas.search.solvers.heuristics.MixedDistanceSumCost;

import org.junit.*;

import java.io.StringReader;

public class CBSSolverTest {
    private final LevelParser levelParser = CourseLevelParser.Instance;
    private Result<Solution> solution;
    private long startTimeMs = 0;

    @Before
    public void setup(){
        IO.logLevel = LogLevel.Information;
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
    
    private Problem getProblem(String level, String... colors){
        var levelWithHeader = String.format("%s\n%s", createLevelHeader(colors), level);
        var parsed = this.levelParser.parse(new StringReader(levelWithHeader));
        Assert.assertTrue(parsed.getErrorMessageOrEmpty(), parsed.isOk());
        return parsed.get();
    }

    private String createLevelHeader(String... colors){
        var colorString = String.join("\n", colors);
        var template = """
                        #domain
                        hospital
                        #levelname
                        test
                        #colors
                        %s
                        """;
    
        return String.format(template, colorString).trim();
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

        var problem = getProblem(level);
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
                    +++++
                    #goal
                    +++++
                    +0 1+
                    ++ ++
                    +++++
                    #end
                    """;
        var problem = getProblem(level, "red: 0", "blue: 1");
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var problem = getProblem(level, "red: 0, 1, 2");
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
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
        var solver = new ConflictBasedSearch(new AStar(new MAAdmissibleCost()));
        solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }
}
