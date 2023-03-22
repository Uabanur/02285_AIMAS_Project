package dtu.aimas;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Color;
import dtu.aimas.common.Position;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;

public class StateTest {
    private Problem makeProblem(List<Agent> agents, List<Box> boxes){
        var x = true; var o = false;
        var walls = new boolean[][] {
            new boolean[] {x, x, x, x, x, x, x},
            new boolean[] {x, o, o, o, x, o, x},
            new boolean[] {x, o, o, o, x, o, x},
            new boolean[] {x, o, o, o, o, o, x},
            new boolean[] {x, x, x, x, x, x, x},
        };
        var goals = new char[walls[0].length][walls.length];
        return new Problem(agents, boxes, walls, goals);
    }
    
    @Test
    public void CreateFromProblem() {
        var problem = makeProblem(List.of(), List.of());
        var stateSpace = ProblemParser.parse(problem);
        Assert.assertTrue("Should be able to create state space from problem", stateSpace.isOk());
    }

    @Test 
    public void SingleAgentInitialState() {
        var agent = new Agent(new Position(1, 1), Color.Red);
        var problem = makeProblem(List.of(agent), List.of());
        var stateSpaceResult = ProblemParser.parse(problem);
        Assert.assertTrue(stateSpaceResult.isOk());
        
        var stateSpace = stateSpaceResult.get();
        var initialState = stateSpace.getInitialState();
        Assert.assertNotNull(initialState);
    
        var agentQuery = stateSpace.getAgentByNumber(initialState, 0);
        // Assert.assertTrue("Agent 0 should exist", agentQuery.isPresent());
        Assert.assertEquals("Agent position should be initial position", agent.pos, agentQuery.pos);
        // Assert.assertTrue("Agent 1 should not exist", stateSpace.getAgentByNumber(initialState, 1).isEmpty());
    }
    
    @Test
    public void ExpandingSingleAgentNoWalls(){
        var agent = new Agent(new Position(2, 2), Color.Red);
        var problem = makeProblem(List.of(agent), List.of());
        var stateSpace = ProblemParser.parse(problem).get();
        var initialState = stateSpace.getInitialState();
        var expanded = stateSpace.expand(initialState);
    
        var expectedPositions = List.of(
            new Position(2, 2),
            new Position(1, 2),
            new Position(3, 2),
            new Position(2, 1),
            new Position(2, 3)
        );

        Assert.assertEquals("Should be 4 open cells and noop", 
            expectedPositions.size(), expanded.size());
        

        // expect only valid expanded positions
        for(var state : expanded) {
            var agent0 = stateSpace.getAgentByNumber(state, 0);
            Assert.assertTrue("Expected valid expanded position", 
                expectedPositions.stream().anyMatch(p -> p.equals(agent0.pos)));
        }
    
        // all expanded positions unique when there are no boxes
        for(var state : expanded) {
            for(var other : expanded ) {
                if (state == other) continue;
                var stateAgent = stateSpace.getAgentByNumber(state, 0);
                var otherAgent = stateSpace.getAgentByNumber(other, 0);
                Assert.assertNotEquals("Expected unique expanded positions", 
                    stateAgent.pos, otherAgent.pos);
            }
        }
    }

    @Test
    public void ExpandingSingleAgentWithWalls() {
        var agent = new Agent(new Position(1, 5), Color.Red);
        var problem = makeProblem(List.of(agent), List.of());
        var stateSpace = ProblemParser.parse(problem).get();
        var initialState = stateSpace.getInitialState();
        var expanded = stateSpace.expand(initialState);
    
        var expectedPositions = List.of(
            new Position(1, 5),
            new Position(2, 5)
        );

        Assert.assertEquals("Should be 1 open cells and noop", 
            expectedPositions.size(), expanded.size());
        

        // expect only valid expanded positions
        for(var state : expanded) {
            var agent0 = stateSpace.getAgentByNumber(state, 0);
            Assert.assertTrue("Expected valid expanded position", 
                expectedPositions.stream().anyMatch(p -> p.equals(agent0.pos)));
        }
    }

    @Test
    public void ExpandingBlockedAgents() {
        var agent0 = new Agent(new Position(1,5), Color.Red);
        var agent1 = new Agent(new Position(2,5), Color.Red);

        var stateSpace = ProblemParser.parse(makeProblem(List.of(agent0, agent1), List.of())).get();
        var initialState = stateSpace.getInitialState();

        Assert.assertEquals(agent0.pos, stateSpace.getAgentByNumber(initialState, 0).pos);
        Assert.assertEquals(agent1.pos, stateSpace.getAgentByNumber(initialState, 1).pos);

        var expanded = stateSpace.expand(initialState);
        var expectedPositionsAgent0 = List.of(
            new Position(1, 5)
        );
        var expectedPositionsAgent1 = List.of(
            new Position(2, 5),
            new Position(3, 5)
        );

        Assert.assertEquals(expectedPositionsAgent0.size() * expectedPositionsAgent1.size(), 
            expanded.size());
    
        for (var state : expanded) {
            var expandedAgent0 = stateSpace.getAgentByNumber(state, 0);
            Assert.assertTrue(expectedPositionsAgent0.stream().anyMatch(p -> 
                p.equals(expandedAgent0.pos)));
            
            var expandedAgent1 = stateSpace.getAgentByNumber(state, 1);
            Assert.assertTrue(expectedPositionsAgent1.stream().anyMatch(p -> 
                p.equals(expandedAgent1.pos)));
        }
    }

    @Test
    public void ExpandingAgentWithBox() {
        var agent = new Agent(new Position(1, 1), Color.Red);
        var box = new Box(new Position(1, 2), Color.Red, 'A');
        var stateSpace = ProblemParser.parse(makeProblem(List.of(agent), List.of(box))).get();
        var initialState = stateSpace.getInitialState();
        var expanded = stateSpace.expand(initialState);
        
        var expectedAgentPositions = List.of(
            new Position(1, 1), // noop
            new Position(1, 2), // push box right
            new Position(1, 2), // push box down
            new Position(2, 1), // go down
            new Position(2, 1)  // go down pulling box
        );

        var expectedBoxPositions = List.of(
            new Position(1, 2), // agent did noop
            new Position(1, 3), // agent pushed box right
            new Position(2, 2), // agent pushed box down
            new Position(1, 2), // agent went down
            new Position(1, 1)  // agent pulled
        );

        Assert.assertEquals(expectedAgentPositions.size(), expanded.size());
    
        loop: for(var state : expanded ){
            for(var i = 0; i < expectedAgentPositions.size(); i++){
                var expectedAgentPosition = expectedAgentPositions.get(i);
                var expectedBoxPosition = expectedBoxPositions.get(i);

                var agentCheck = expectedAgentPosition.equals(stateSpace.getAgentByNumber(state, 0).pos);
                var boxCheck = stateSpace.getBoxAt(state, expectedBoxPosition).isPresent();

                if(agentCheck && boxCheck) continue loop;
            }
            Assert.fail("Agent and box was not found at expected positions");
        }
    }
}
