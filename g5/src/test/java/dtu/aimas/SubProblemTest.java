package dtu.aimas;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Color;
import dtu.aimas.common.Position;
import dtu.aimas.search.Problem;

public class SubProblemTest {
    
    @Test
    public void SubProblem_Of_TwoAgents_Different_Color(){
        var height = 10;
        var width = 10;

        var agents = List.of(
            new Agent(new Position(1, 1), Color.Red, '0'),
            new Agent(new Position(2, 1), Color.Blue, '1')
        );

        var boxes = List.of(
            new Box(new Position(1, 2), Color.Red, 'A'),
            new Box(new Position(2, 2), Color.Blue, 'B')
        );

        var walls = new boolean[width][height];
        var goals = new char[width][height];
        for(var agent: agents) goals[agent.pos.row][agent.pos.col] = agent.label;
        for(var box: boxes) goals[box.pos.row][box.pos.col] = box.label;

        var problem = new Problem(agents, boxes, walls, goals);
        var expectedAgents = new Agent[]{agents.get(0)};
        var expectedBoxes = new Box[]{boxes.get(0)};

        var subProblem = problem.subProblemFor(agents.get(0));
        Assert.assertArrayEquals(expectedAgents, subProblem.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes, subProblem.boxes.toArray(Box[]::new));

        for(var row = 0; row < goals.length; row++) {
            for(var col = 0; col < goals[row].length; col++) {
                var symbol = goals[row][col];
                if (symbol == 0) continue;
                if (Stream.of(expectedAgents).anyMatch(a -> a.label == symbol)) continue;
                if (Stream.of(expectedBoxes).anyMatch(b -> b.label == symbol)) continue;
                Assert.fail("Goal found not belonging to subproblem. Goal type: " + symbol);
            }
        }
    }

    @Test
    public void SubProblem_Of_TwoAgents_Same_Color(){
        var height = 10;
        var width = 10;

        var agents = List.of(
            new Agent(new Position(1, 1), Color.Red, '0'),
            new Agent(new Position(2, 1), Color.Red, '1')
        );

        var boxes = List.of(
            new Box(new Position(1, 2), Color.Red, 'A'),
            new Box(new Position(2, 2), Color.Red, 'B')
        );

        var walls = new boolean[width][height];
        var goals = new char[width][height];
        for(var agent: agents) goals[agent.pos.row][agent.pos.col] = agent.label;
        for(var box: boxes) goals[box.pos.row][box.pos.col] = box.label;

        var problem = new Problem(agents, boxes, walls, goals);
        var expectedAgents = new Agent[]{agents.get(0)};
        var expectedBoxes = boxes.toArray(Box[]::new);

        var subProblem = problem.subProblemFor(agents.get(0));
        Assert.assertArrayEquals(expectedAgents, subProblem.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes, subProblem.boxes.toArray(Box[]::new));

        for(var row = 0; row < goals.length; row++) {
            for(var col = 0; col < goals[row].length; col++) {
                var symbol = goals[row][col];
                if (symbol == 0) continue;
                if (Stream.of(expectedAgents).anyMatch(a -> a.label == symbol)) continue;
                if (Stream.of(expectedBoxes).anyMatch(b -> b.label == symbol)) continue;
                Assert.fail("Goal found not belonging to subproblem. Goal type: " + symbol);
            }
        }
    }
}
