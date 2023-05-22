package dtu.aimas;

import dtu.aimas.common.Position;
import dtu.aimas.helpers.LevelHelper;
import dtu.aimas.search.solvers.safeinterval.ReservedCell;
import dtu.aimas.search.solvers.safeinterval.SafeProblem;
import dtu.aimas.search.solvers.safeinterval.TimeInterval;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SafeProblemTest {

    @Test
    public void BleedingConstraintsTest(){
        var level = """
                #initial
                ++++++
                +    +
                +    +
                +    +
                ++++++
                #goal
                ++++++
                +    +
                +    +
                +    +
                ++++++
                #end
                """;

        var problem = LevelHelper.getProblem(level, "red: 0");

        var intervals1 = List.of(new ReservedCell(new Position(0, 0), new TimeInterval(0, 1)));
        var safeProblem = SafeProblem.from(problem, intervals1).orElseThrow();

        var intervals2 = List.of(new ReservedCell(new Position(0, 0), new TimeInterval(1, 2)));
        var safeProblem2 = SafeProblem.from(safeProblem, intervals2).orElseThrow();

        Assert.assertTrue(true);
    }
}
