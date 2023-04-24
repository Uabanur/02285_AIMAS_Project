package dtu.aimas;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Color;
import dtu.aimas.common.Position;
import dtu.aimas.communication.IO;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.conflictbasedsearch.CBSNode;
import dtu.aimas.search.solvers.graphsearch.BFS;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class ConflictsTest {
    
    // TODO: Fix this test
    // @Test
    // public void FindFirstConflict_TwoAgents_NoBoxes(){

    //     // Arrange: Setup the problem with conflicting sub-solutions
    //     var height = 10;
    //     var width = 10;

    //     var agents = new ArrayList<Agent>();
    //     agents.add(new Agent(new Position(4, 5), Color.Red, '0'));
    //     agents.add(new Agent(new Position(5, 4), Color.Blue, '1'));
        
    //     var boxes = new ArrayList<Box>();

    //     var walls = new boolean[width][height];
    //     var goals = new char[width][height];
    //     goals[6][5] = '0';
    //     goals[5][6] = '1';

    //     var problem = new Problem(agents, boxes, walls, goals);
    //     var subSolver = new BFS();

    //     var node = new CBSNode();
    //     for(var agent : problem.agents){
    //         var isolatedSolution = subSolver.solve(problem.subProblemFor(agent));
    //         node.setSolutionFor(agent, isolatedSolution);
    //     }

    //     // Act: Find the first conflict
    //     var initialState = new State(agents, boxes);
    //     var stateSpace = new StateSpace(problem, initialState);
    //     var conflict = node.findConflicts(stateSpace).get(0);

    //     IO.info(conflict);

    //     // Assert conflict's position, agents and timestep
    //     var expectedPosition = new Position(5, 5);
    //     var expectedInvolvedAgents = agents.toArray(new Agent[0]);
    //     var expectedTimestep = 0;

    //     var involvedAgents = conflict.getInvolvedAgents();
    //     for (var i = 0; i < involvedAgents.length; i++) {
    //         Assert.assertEquals(involvedAgents[i].label, expectedInvolvedAgents[i].label);
    //     }
    //     Assert.assertTrue(expectedTimestep == conflict.getTimeStep());
    //     Assert.assertTrue(
    //         expectedPosition.row == conflict.getPosition().row && 
    //         expectedPosition.col == conflict.getPosition().col
    //     );
    // }
    
}
