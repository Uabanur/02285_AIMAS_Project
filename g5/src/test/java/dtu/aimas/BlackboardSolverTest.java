package dtu.aimas;

import java.io.StringReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.blackboard.BlackboardSolver;
import dtu.aimas.search.solvers.graphsearch.AStar;

public class BlackboardSolverTest {
    private LevelParser levelParser = CourseLevelParser.Instance;
    private Solver solver;

    @Before
    public void setup(){
        solver = new BlackboardSolver(AStar::new);
    }
    
    private Problem getProblem(String level, String... colors){
        var levelWithHeader = String.format("%s\n%s", createLevelHeader(colors), level);
        var parsed = this.levelParser.parse(new StringReader(levelWithHeader));
        Assert.assertTrue(parsed.getErrorMessageOrEmpty(), parsed.isOk());
        return parsed.get();
    }

    private String createLevelHeader(String... colors){
        var colorString = String.join("\n", colors);
        var template = 
"""
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
        var level = 
"""
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
        var solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
    }

    @Test 
    public void SingleAgent1Box() {
        var level = 
"""
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
        var solution = solver.solve(problem);
        Assert.assertTrue(solution.isOk());
        // TODO: Validate that solution is valid in statespace
    }

    @Test
    public void TwoAgentsNoConflict(){
        var level = 
"""
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
        var solution = solver.solve(problem);
        Assert.assertTrue(solution.toString(), solution.isOk());
    }

    @Test
    public void TwoAgents_Crossing(){
        var level = 
"""
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
        var solution = solver.solve(problem);
        // Assert.assertTrue(solution.isOk());
    }
}
