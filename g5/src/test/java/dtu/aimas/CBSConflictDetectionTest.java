package dtu.aimas;

import dtu.aimas.common.*;
import dtu.aimas.search.Action;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.conflictbasedsearch.Conflict;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

import org.junit.*;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;



public class CBSConflictDetectionTest {
    final int step = 1;
    final char arbitraryBoxLabel = 'A';

    // default agents setup

    public class AgentsBuilder {
        private ArrayList<Agent> agents = new ArrayList<Agent>();

        public AgentsBuilder addAgent(Position pos, Color col){
            agents.add(new Agent(pos, col, (char)('0' + agents.size())));
            return this;
        }

        public ArrayList<Agent> build(){
            return agents;
        }
    }

    // default boxes setup

    public class BoxesBuilder {
        private ArrayList<Box> boxes = new ArrayList<Box>();

        public BoxesBuilder addBox(Position pos, Color col){
            boxes.add(new Box(pos, col, arbitraryBoxLabel));
            return this;
        }

        public ArrayList<Box> build(){
            return boxes;
        }
    }

    // default actions setup

    public class ActionsBuilder {
        private ArrayList<Action> actions = new ArrayList<Action>();

        public ActionsBuilder addAction(Action action){
            actions.add(action);
            return this;
        }

        public Action[] build(){
            return actions.toArray(new Action[actions.size()]);
        }
    }

    // default state space setup

    private StateSpace setUpStateSpace(ArrayList<Agent> agents, ArrayList<Box> boxes)
    {
        var initialState = new State(agents, boxes);
        var stateSpace = new StateSpace(mock(Problem.class), initialState);
        return stateSpace;
    }

    // conflict parameters assertion
    
    private void assertConflictParameters(Position expectedConflictingPosition, ArrayList<Agent> expectedConflictingAgents, Conflict conflict){
        Assert.assertTrue(expectedConflictingPosition.equals(conflict.getPosition()));

        var conflictingAgents = conflict.getInvolvedAgents();
        Assert.assertEquals(expectedConflictingAgents.size(), conflictingAgents.size());

        for(var expectedConflictingAgent : expectedConflictingAgents)
            Assert.assertTrue(conflictingAgents.stream().anyMatch(a -> a.label == expectedConflictingAgent.label));
    }

    // test cases:
    // no conflict

    @Test
    public void NoConflict() {
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.NoOp;

        var agent1_pos = new Position(0, 1);
        var agent1_action = Action.NoOp;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertFalse(possibleConflict.isPresent());
    }

    // one agent conflicts

    @Test
    public void OneAgent_DestinationOccupiedByHisBox(){
        var agent0_pos = new Position(0, 0);
        var box0_pos = new Position(0, 1);
        var agent0_action = Action.MoveE;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).build();
        var boxes = new BoxesBuilder().addBox(box0_pos, Color.Blue).build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }
    
    @Test
    public void OneAgent_DestinationOccupiedByNobodysBox(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 2);
        var box1_pos = new Position(0, 1);
        var agent1_action = Action.NoOp; // whatever

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().addBox(box1_pos, Color.Grey).build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).build();
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    // two agents conflicts

    @Test
    public void TwoAgents_SameDestination(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 2);
        var agent1_action = Action.MoveW;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    @Test
    public void TwoAgents_OneOccupyingDestination(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 1);
        var agent1_action = Action.NoOp;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    @Test
    public void TwoAgents_DestinationOccupiedBySomebodyElsesBox(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 2);
        var box1_pos = new Position(0, 1);
        var agent1_action = Action.NoOp; // whatever

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().addBox(box1_pos, Color.Green).build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    @Test
    public void TwoAgents_AgentFollowingAgentConflict(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 1);
        var agent1_action = Action.MoveE;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    @Test
    public void TwoAgents_BoxFollowingAgentConflict(){
        var agent0_pos = new Position(0, 0);
        var box0_pos = new Position(0, 1);
        var agent0_action = Action.PushEE;

        var agent1_pos = new Position(0, 2);
        var agent1_action = Action.MoveE;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().addBox(box0_pos, Color.Blue).build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 2);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    @Test
    public void TwoAgents_AgentFollowingBoxConflict(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 2);
        var box1_pos = new Position(0, 1);
        var agent1_action = Action.PullEE;


        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().addBox(box1_pos, Color.Green).build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    @Test
    public void TwoAgents_BoxFollowingBoxConflict(){
        var agent0_pos = new Position(0, 0);
        var box0_pos = new Position(0, 1);
        var agent0_action = Action.PushEE;

        var agent1_pos = new Position(0, 3);
        var box1_pos = new Position(0, 2);
        var agent1_action = Action.PullEE;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var boxes = new BoxesBuilder().addBox(box0_pos, Color.Blue).addBox(box1_pos, Color.Green).build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 2);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    // three agents conflicts

    @Test
    public void ThreeAgents_SameDestination(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 2);
        var agent1_action = Action.MoveW;

        var agent2_pos = new Position(1, 1);
        var agent2_action = Action.MoveN;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).addAgent(agent2_pos, Color.Cyan).build();
        var boxes = new BoxesBuilder().build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).addAction(agent2_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    @Test
    public void ThreeAgents_OneWithBox(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 2);
        var agent1_action = Action.MoveW;

        var agent2_pos = new Position(2, 1);
        var box2_pos = new Position(1, 1);
        var agent2_action = Action.PushNN;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).addAgent(agent2_pos, Color.Cyan).build();
        var boxes = new BoxesBuilder().addBox(box2_pos, Color.Cyan).build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).addAction(agent2_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = agents;
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }

    @Test
    public void ThreeAgents_ButOneNotIncludedInConflict(){
        var agent0_pos = new Position(0, 0);
        var agent0_action = Action.MoveE;

        var agent1_pos = new Position(0, 1);
        var agent1_action = Action.NoOp;

        var agent2_pos = new Position(0, 2);
        var agent2_action = Action.NoOp;

        var agents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).addAgent(agent2_pos, Color.Cyan).build();
        var boxes = new BoxesBuilder().build();
        var jointAction = new ActionsBuilder().addAction(agent0_action).addAction(agent1_action).addAction(agent2_action).build();
        var stateSpace = setUpStateSpace(agents, boxes);

        var expectedConflictingAgents = new AgentsBuilder().addAgent(agent0_pos, Color.Blue).addAgent(agent1_pos, Color.Green).build();
        var expectedConflictingPosition = new Position(0, 1);

        var conflictingState = stateSpace.applyJointActions(stateSpace.initialState(), jointAction);
        var possibleConflict = stateSpace.tryGetConflict(conflictingState, step);
        
        Assert.assertTrue(possibleConflict.isPresent());
        assertConflictParameters(expectedConflictingPosition, expectedConflictingAgents, possibleConflict.get());
    }
    
}
