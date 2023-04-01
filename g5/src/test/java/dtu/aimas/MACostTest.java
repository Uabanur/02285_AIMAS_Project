package dtu.aimas;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Position;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.MAAdmissibleCost;
import dtu.aimas.common.Color;
import dtu.aimas.common.Box;

public class MACostTest {
    private static MAAdmissibleCost cost = new MAAdmissibleCost();
    @Test
    public void singleChoiceCost(){
        var width = 20; 
        var height = 10;
        var goals = new char[height][width];
        var walls = new boolean[height][width];
        // Using a single agent, box and goal
        var agents = new Agent[]{
            new Agent(new Position(0,0), Color.Blue, '0')
        };
        var boxes = new Box[]{
            new Box(new Position(0,5), Color.Blue, 'A')
        };
        
        var tests = new Position[][] {
            // Agent, Box, Goal and AgentGoal positions
            new Position[] {new Position(0,0), new Position(0,5), new Position(0,10), new Position(-1,-1)},
            new Position[] {new Position(0,0), new Position(0,5), new Position(0,2), new Position(0,0)},
            new Position[] {new Position(5,5), new Position(0,0), new Position(2,2), new Position(9,0)}
        };

        for(var test : tests) {
            agents[0].pos = test[0];
            boxes[0] = new Box(test[1], Color.Blue, 'A'); //Box.pos is final, so this is necessary
            goals[test[2].row][test[2].col] = 'A';  //Box goal
            if(test[3].row >= 0) goals[test[3].row][test[3].col] = '0';
            Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
            StateSpace space = ProblemParser.parse(problem).get();
            State state = space.getInitialState();

            int expected = problem.admissibleDist(test[0], test[1]) + problem.admissibleDist(test[1], test[2]);
            if(test[3].row >= 0) expected += problem.admissibleDist(test[2], test[3]);
            Assert.assertEquals(expected, cost.calculate(state, space));
            
            //Clear the map
            goals[test[2].row][test[2].col] = 0;  //Box goal
            if(test[3].row >= 0) goals[test[3].row][test[3].col] = 0;
        }

    }
}
