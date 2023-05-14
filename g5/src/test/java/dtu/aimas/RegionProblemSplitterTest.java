package dtu.aimas;

import dtu.aimas.common.*;
import dtu.aimas.search.problems.RegionProblemSplitter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static dtu.aimas.helpers.LevelHelper.getProblem;

public class RegionProblemSplitterTest {
    private RegionProblemSplitter splitter;

    @Before
    public void setup()
    {
        this.splitter = new RegionProblemSplitter();
    }

    @Test
    public void SingleRegionTest(){
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
        var split = splitter.split(problem);
        Assert.assertEquals("Expected single region", 1, split.size());
    }

    @Test
    public void TwoRegionTest(){
        var level = """
                    #initial
                    +++++
                    + + +
                    +++++
                    #goal
                    +++++
                    + + +
                    +++++
                    #end
                    """;

        var problem = getProblem(level, "red:0");
        var split = splitter.split(problem);
        Assert.assertEquals("Expected two regions", 2, split.size());
    }

    @Test
    public void TwoRegionWithAgentsTest(){
        var level = """
                    #initial
                    +++++
                    +0+1+
                    +++++
                    #goal
                    +++++
                    +0+1+
                    +++++
                    #end
                    """;

        var problem = getProblem(level, "red:0");
        var split = splitter.split(problem);
        Assert.assertEquals("Expected two regions", 2, split.size());

        var firstRegion = split.get(0);
        Assert.assertEquals(1, firstRegion.agents.size());
        Assert.assertArrayEquals(new Character[]{'0'}, firstRegion.agents.stream().map(a -> a.label).toArray(Character[]::new));
        Assert.assertArrayEquals(new Character[]{'0'}, firstRegion.agentGoals.stream().map(a -> a.label).toArray(Character[]::new));

        var secondRegion = split.get(1);
        Assert.assertEquals(1, secondRegion.agents.size());
        Assert.assertArrayEquals(new Character[]{'1'}, secondRegion.agents.stream().map(a -> a.label).toArray(Character[]::new));
        Assert.assertArrayEquals(new Character[]{'1'}, secondRegion.agentGoals.stream().map(a -> a.label).toArray(Character[]::new));
    }

    @Test
    public void AgentAndBoxRegions(){
        var level = """
                    #initial
                    ++++++++++++++++
                    +    +4AB +5  6+
                    +    + ++++C++C+
                    +0123+    + ++ +
                    +    ++++ + ++ +
                    +    +CCCC+    +
                    ++++++++++++++++
                    #goal
                    ++++++++++++++++
                    +    +    +    +
                    +    + ++++ ++ +
                    +0123+4AB + ++ +
                    +    ++++ + ++ +
                    +    +CCCC+C  C+
                    ++++++++++++++++
                    #end
                    """;

        var problem = getProblem(level,
                "red: 0,1,",
                "green: 2,3",
                "blue:4,A,B",
                "cyan:5,6,C"
        );
        var split = splitter.split(problem);
        Assert.assertEquals("Expected three regions", 3, split.size());

        {   // left region
            var region = split.get(0);
            var expectedAgents = List.of(
                    new Agent(new Position(3, 1), Color.Red, '0'),
                    new Agent(new Position(3, 2), Color.Red, '1'),
                    new Agent(new Position(3, 3), Color.Green, '2'),
                    new Agent(new Position(3, 4), Color.Green, '3')
            );

            Assert.assertEquals(expectedAgents.size(), region.agents.size());
            Assert.assertTrue(expectedAgents.containsAll(region.agents));

            var expectedAgentGoals = List.of(
                    new Goal('0', new Position(3, 1)),
                    new Goal('1', new Position(3, 2)),
                    new Goal('2', new Position(3, 3)),
                    new Goal('3', new Position(3, 4))
            );

            Assert.assertEquals(expectedAgentGoals.size(), region.agentGoals.size());
            Assert.assertTrue(expectedAgentGoals.containsAll(region.agentGoals));
        }
        {   // middle region
            var region = split.get(1);
            var expectedAgents = List.of(
                    new Agent(new Position(1, 6), Color.Blue, '4')
            );

            Assert.assertEquals(expectedAgents.size(), region.agents.size());
            Assert.assertTrue(expectedAgents.containsAll(region.agents));

            var expectedAgentGoals = List.of(
                    new Goal('4', new Position(3, 6))
            );

            Assert.assertEquals(expectedAgentGoals.size(), region.agentGoals.size());
            Assert.assertTrue(expectedAgentGoals.containsAll(region.agentGoals));

            var expectedBoxes = List.of(
                    new Box(new Position(1, 7), Color.Blue, 'A'),
                    new Box(new Position(1, 8), Color.Blue, 'B'),

                    new Box(new Position(5, 6), Color.Cyan, 'C'),
                    new Box(new Position(5, 7), Color.Cyan, 'C'),
                    new Box(new Position(5, 8), Color.Cyan, 'C'),
                    new Box(new Position(5, 9), Color.Cyan, 'C')
            );

            Assert.assertEquals(expectedBoxes.size(), region.boxes.size());
            Assert.assertTrue(expectedBoxes.containsAll(region.boxes));

            var expectedBoxGoals = List.of(
                    new Goal('A', new Position(3, 7)),
                    new Goal('B', new Position(3, 8)),

                    new Goal('C', new Position(5, 6)),
                    new Goal('C', new Position(5, 7)),
                    new Goal('C', new Position(5, 8)),
                    new Goal('C', new Position(5, 9))
            );

            Assert.assertEquals(expectedBoxGoals.size(), region.boxGoals.size());
            Assert.assertTrue(expectedBoxGoals.containsAll(region.boxGoals));
        }
        {   // right region
            var region = split.get(2);
            var expectedAgents = List.of(
                    new Agent(new Position(1, 11), Color.Cyan, '5'),
                    new Agent(new Position(1, 14), Color.Cyan, '6')
            );

            Assert.assertEquals(expectedAgents.size(), region.agents.size());
            Assert.assertTrue(expectedAgents.containsAll(region.agents));

            Assert.assertEquals(0, region.agentGoals.size());

            var expectedBoxes = List.of(
                    new Box(new Position(2, 11), Color.Cyan, 'C'),
                    new Box(new Position(2, 14), Color.Cyan, 'C')
            );

            Assert.assertEquals(expectedBoxes.size(), region.boxes.size());
            Assert.assertTrue(expectedBoxes.containsAll(region.boxes));

            var expectedBoxGoals = List.of(
                    new Goal('C', new Position(5, 11)),
                    new Goal('C', new Position(5, 14))
            );

            Assert.assertEquals(expectedBoxGoals.size(), region.boxGoals.size());
            Assert.assertTrue(expectedBoxGoals.containsAll(region.boxGoals));
        }
    }
}
