package dtu.aimas;

import java.util.Arrays;
import java.util.Collections;

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

    private int singleAgentBoxGoalCost(Problem problem, Agent agent, Box box, Position goalPos, Position agentGoalPos) {
        int agentBoxDist = problem.admissibleDist(agent.pos, box.pos);
        if(agentBoxDist == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        int boxGoalDist = problem.admissibleDist(box.pos, goalPos);
        if(boxGoalDist == Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if(agentGoalPos == null) {
            return agentBoxDist +  boxGoalDist;
        }
        else {
            int agentGoalDist = problem.admissibleDist(goalPos, agentGoalPos);
            if(agentGoalDist == Integer.MAX_VALUE) return Integer.MAX_VALUE;
            return agentBoxDist + boxGoalDist + agentGoalDist;
        }
    }   

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
            // Agent, Box, Goal and AgentGoal positions (if agentGoal is -1, no agentGoal)
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

            //agent -> box -> boxGoal (-> agentGoal)
            int expected = problem.admissibleDist(test[0], test[1]) + problem.admissibleDist(test[1], test[2]);
            if(test[3].row >= 0) expected += problem.admissibleDist(test[2], test[3]);
            Assert.assertEquals(expected, cost.calculate(state, space));
            
            //Clear the map
            goals[test[2].row][test[2].col] = 0;  //Box goal
            if(test[3].row >= 0) goals[test[3].row][test[3].col] = 0;
        }
    }

    //Test 1: 2 agents with different colors
    private void test1(int width, int height, boolean[][] walls) {
        var goals = new char[height][width];
        var agents = new Agent[]{
            new Agent(new Position(0,0), Color.Blue, '0'),
            new Agent(new Position(9, 19), Color.Red, '1')
        };
        var boxes = new Box[]{
            new Box(new Position(0,5), Color.Blue, 'A'),
            new Box(new Position(1, 1), Color.Red, 'B')
        };
        Position goalA = new Position(0, 6); goals[goalA.row][goalA.col] = 'A';
        Position goalB = new Position(4, 19); goals[goalB.row][goalB.col] = 'B';
        Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
        StateSpace space = ProblemParser.parse(problem).get();
        State state = space.getInitialState();

        int boxGoal0Cost = singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA, null);
        int boxGoal1Cost = singleAgentBoxGoalCost(problem, agents[1], boxes[1], goalB, null);
        //Cost should be that of the longest goal to complete
        int expected = Math.max(boxGoal0Cost, boxGoal1Cost);
        Assert.assertEquals(expected, cost.calculate(state, space));
    }

    //Test 2: 2 agents with same colors and one goal
    private void test2(int width, int height, boolean[][] walls) {
        var goals = new char[height][width];
        var agents = new Agent[]{
            new Agent(new Position(0,0), Color.Blue, '0'),
            new Agent(new Position(9, 19), Color.Blue, '1')
        };
        var boxes = new Box[]{
            new Box(new Position(0,5), Color.Blue, 'A')
        };
        Position goalA = new Position(0, 6); goals[goalA.row][goalA.col] = 'A';
        Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
        StateSpace space = ProblemParser.parse(problem).get();
        State state = space.getInitialState();

        int agent0Goal = singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA, null);
        int agent1Goal = singleAgentBoxGoalCost(problem, agents[1], boxes[0], goalA, null);
        //Cost should be the cost of the most fit agent to solve
        int expected = Math.min(agent0Goal, agent1Goal);
        Assert.assertEquals(expected, cost.calculate(state, space));
    }

    //Test 3: 2 agents with same colors, one goal but two box
    private void test3(int width, int height, boolean[][] walls) {
        var goals = new char[height][width];
        var agents = new Agent[]{
            new Agent(new Position(0,0), Color.Blue, '0'),
            new Agent(new Position(9, 19), Color.Blue, '1')
        };
        var boxes = new Box[]{
            new Box(new Position(0,5), Color.Blue, 'A'),
            new Box(new Position(9, 5), Color.Blue, 'A')
        };
        Position goalA = new Position(8, 15); goals[goalA.row][goalA.col] = 'A';
        Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
        StateSpace space = ProblemParser.parse(problem).get();
        State state = space.getInitialState();

        int agent0GoalCost = Math.min(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA, null)
        );
        int agent1GoalCost = Math.min(
            singleAgentBoxGoalCost(problem, agents[1], boxes[0], goalA, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[1], goalA, null)
        );
        //Cost should be the cost of the most fit agent to solve the goal
        int expected = Math.min(agent0GoalCost, agent1GoalCost);
        Assert.assertEquals(expected, cost.calculate(state, space));
    }

    //Test 4: 2 agents with same colors, 2 goals and multiple boxes
    private void test4(int width, int height, boolean[][] walls) {
        var goals = new char[height][width];
        var agents = new Agent[]{
            new Agent(new Position(0,0), Color.Blue, '0'),
            new Agent(new Position(9, 19), Color.Blue, '1')
        };
        var boxes = new Box[]{
            new Box(new Position(0, 5), Color.Blue, 'A'),
            new Box(new Position(9, 5), Color.Blue, 'A'),
            new Box(new Position(3, 12), Color.Blue, 'A'),
            new Box(new Position(4, 0), Color.Blue, 'A')
        };
        Position goalA0 = new Position(8, 15); goals[goalA0.row][goalA0.col] = 'A';
        Position goalA1 = new Position(4, 9); goals[goalA1.row][goalA1.col] = 'A';
        Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
        StateSpace space = ProblemParser.parse(problem).get();
        State state = space.getInitialState();

        int minGoalA0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[2], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[3], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[0], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[1], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[2], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA0, null)
        ));
        int minGoalA1Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[2], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[3], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[0], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[1], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[2], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA1, null)
        ));
        //Cost should be the cost of the most fit agent to solve the goal
        int expected = Math.max(minGoalA0Cost, minGoalA1Cost);
        Assert.assertEquals(expected, cost.calculate(state, space));
    }

    //test 5: 2 agents with different colors, one goal and multiple boxes (with same label) for each
    private void test5(int width, int height, boolean[][] walls) {
        var goals = new char[height][width];
        var agents = new Agent[]{
            new Agent(new Position(0,0), Color.Blue, '0'),
            new Agent(new Position(9, 19), Color.Red, '1')
        };
        var boxes = new Box[]{
            new Box(new Position(0, 5), Color.Blue, 'A'),
            new Box(new Position(9, 5), Color.Blue, 'A'),
            new Box(new Position(3, 12), Color.Red, 'A'),
            new Box(new Position(4, 0), Color.Red, 'A')
        };
        Position goalA0 = new Position(8, 15); goals[goalA0.row][goalA0.col] = 'A';
        Position goalA1 = new Position(4, 9); goals[goalA1.row][goalA1.col] = 'A';
        Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
        StateSpace space = ProblemParser.parse(problem).get();
        State state = space.getInitialState();

        int minGoalA0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[2], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA0, null)
        ));
        int minGoalA1Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[2], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA1, null)
        ));
        //Cost should be the cost of the most fit agent to solve the goal
        int expected = Math.max(minGoalA0Cost, minGoalA1Cost);
        Assert.assertEquals(expected, cost.calculate(state, space));
    }

    //test 6: 2 agents with different colors, one goal and multiple boxes (with different label) for each
    private void test6(int width, int height, boolean[][] walls) {
        var goals = new char[height][width];
        var agents = new Agent[]{
            new Agent(new Position(5, 5), Color.Blue, '0'),
            new Agent(new Position(3, 3), Color.Red, '1')
        };
        var boxes = new Box[]{
            new Box(new Position(8, 7), Color.Blue, 'A'),
            new Box(new Position(8, 19), Color.Blue, 'A'),
            new Box(new Position(9, 18), Color.Blue, 'B'),
            new Box(new Position(2, 3), Color.Red, 'A'),
            new Box(new Position(0, 0), Color.Red, 'B'),
            new Box(new Position(4, 9), Color.Red, 'B')
        };
        Position goalA0 = new Position(8, 15); goals[goalA0.row][goalA0.col] = 'A';
        Position goalB0 = new Position(4, 9); goals[goalB0.row][goalB0.col] = 'B';
        Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
        StateSpace space = ProblemParser.parse(problem).get();
        State state = space.getInitialState();

        int minGoalA0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA0, null)
        ));
        int minGoalB0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[2], goalB0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[4], goalB0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[5], goalB0, null)
        ));
        //Cost should be the cost of the most fit agent to solve the goal
        int expected = Math.max(minGoalA0Cost, minGoalB0Cost);
        Assert.assertEquals(expected, cost.calculate(state, space));
    }

    //test 7: 2 agents with different colors, multiple goals and boxes for each
    private void test7(int width, int height, boolean[][] walls) {
        var goals = new char[height][width];
        var agents = new Agent[]{
            new Agent(new Position(5, 5), Color.Blue, '0'),
            new Agent(new Position(3, 3), Color.Red, '1')
        };
        var boxes = new Box[]{
            new Box(new Position(8, 7), Color.Blue, 'A'),
            new Box(new Position(8, 19), Color.Blue, 'A'),
            new Box(new Position(9, 18), Color.Blue, 'B'),
            new Box(new Position(2, 3), Color.Red, 'A'),
            new Box(new Position(0, 0), Color.Red, 'B'),
            new Box(new Position(4, 9), Color.Red, 'B'),
            new Box(new Position(4, 9), Color.Red, 'C')
        };
        Position goalA0 = new Position(8, 15); goals[goalA0.row][goalA0.col] = 'A';
        Position goalA1 = new Position(2, 8); goals[goalA1.row][goalA1.col] = 'A';
        Position goalB0 = new Position(4, 9); goals[goalB0.row][goalB0.col] = 'B';
        Position goalC0 = new Position(9, 9); goals[goalC0.row][goalC0.col] = 'C';
        Position agentGoal0 = new Position(0,0); goals[agentGoal0.row][agentGoal0.col] = '0';
        Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
        StateSpace space = ProblemParser.parse(problem).get();
        State state = space.getInitialState();

        int minGoalA0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA0, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA0, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA0, null)
        ));
        int minGoalA1Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA1, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA1, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA1, null)
        ));
        int minGoalB0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[2], goalB0, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[1], boxes[4], goalB0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[5], goalB0, null)
        ));
        int minGoalC0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[1], boxes[6], goalC0, null)
        ));
        //Cost should be the cost of the most fit agent to solve the goal
        int expected = Collections.max(Arrays.asList(
            minGoalA0Cost, minGoalA1Cost, minGoalB0Cost, minGoalC0Cost
        ));
        Assert.assertEquals(expected, cost.calculate(state, space));
    }

    //test 8: multiple agents with same and different colors, goals and boxes
    private void test8(int width, int height, boolean[][] walls) {
        var goals = new char[height][width];
        var agents = new Agent[]{
            new Agent(new Position(5, 5), Color.Blue, '0'),
            new Agent(new Position(3, 3), Color.Red, '1'),
            new Agent(new Position(9, 18), Color.Red, '2')
        };
        var boxes = new Box[]{
            new Box(new Position(8, 7), Color.Blue, 'A'),
            new Box(new Position(8, 19), Color.Blue, 'A'),
            new Box(new Position(9, 18), Color.Blue, 'B'),
            new Box(new Position(2, 3), Color.Red, 'A'),
            new Box(new Position(0, 0), Color.Red, 'B'),
            new Box(new Position(4, 9), Color.Red, 'B'),
            new Box(new Position(4, 9), Color.Red, 'C'),
            new Box(new Position(6, 8), Color.Red, 'C')
        };
        Position goalA0 = new Position(8, 15); goals[goalA0.row][goalA0.col] = 'A';
        Position goalA1 = new Position(2, 8); goals[goalA1.row][goalA1.col] = 'A';
        Position goalB0 = new Position(4, 9); goals[goalB0.row][goalB0.col] = 'B';
        Position goalC0 = new Position(9, 9); goals[goalC0.row][goalC0.col] = 'C';
        Position agentGoal0 = new Position(0,0); goals[agentGoal0.row][agentGoal0.col] = '0';
        Position agentGoal2 = new Position(5,5); goals[agentGoal2.row][agentGoal2.col] = '2';
        Problem problem = new Problem(Arrays.asList(agents), Arrays.asList(boxes), walls, goals).precompute();
        StateSpace space = ProblemParser.parse(problem).get();
        State state = space.getInitialState();

        int minGoalA0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA0, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA0, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA0, null),
            singleAgentBoxGoalCost(problem, agents[2], boxes[3], goalA0, agentGoal2)
        ));
        int minGoalA1Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[0], goalA1, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[0], boxes[1], goalA1, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[1], boxes[3], goalA1, null),
            singleAgentBoxGoalCost(problem, agents[2], boxes[3], goalA1, agentGoal2)
        ));
        int minGoalB0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[0], boxes[2], goalB0, agentGoal0),
            singleAgentBoxGoalCost(problem, agents[1], boxes[4], goalB0, null),
            singleAgentBoxGoalCost(problem, agents[1], boxes[5], goalB0, null),
            singleAgentBoxGoalCost(problem, agents[2], boxes[4], goalB0, agentGoal2),
            singleAgentBoxGoalCost(problem, agents[2], boxes[5], goalB0, agentGoal2)
        ));
        int minGoalC0Cost = Collections.min(Arrays.asList(
            singleAgentBoxGoalCost(problem, agents[1], boxes[6], goalC0, null),
            singleAgentBoxGoalCost(problem, agents[2], boxes[6], goalC0, agentGoal2)
        ));
        //Cost should be the cost of the most fit agent to solve the goal
        int expected = Collections.max(Arrays.asList(
            minGoalA0Cost, minGoalA1Cost, minGoalB0Cost, minGoalC0Cost
        ));
        Assert.assertEquals(expected, cost.calculate(state, space));
    }


    @Test
    public void multipleChoiceCost() {
        var width = 20; 
        var height = 10;
        var walls = new boolean[height][width];
                      
        test1(width, height, walls);
        test2(width, height, walls);
        test3(width, height, walls);
        test4(width, height, walls);
        test5(width, height, walls);
        test6(width, height, walls);
        test7(width, height, walls);
        test8(width, height, walls);
    }

    @Test
    public void costsWithWalls01() {
        //mix of multiple choice and single choice. 
        //Not as thorough, as wall distances are tested in the precomputed distance tests
        var width = 20; 
        var height = 10;
        var walls = new boolean[height][width];
        walls[0][10] = true;
        walls[1][10] = true;
        walls[2][10] = true;
        walls[3][10] = true;
        walls[4][10] = true;
        walls[6][10] = true;
        walls[7][10] = true;
        walls[8][10] = true;
        walls[9][10] = true;

        test1(width, height, walls);
        test2(width, height, walls);
        test3(width, height, walls);
        test4(width, height, walls);
        test5(width, height, walls);
        test6(width, height, walls);
        test7(width, height, walls);
        test8(width, height, walls);
    }

    @Test
    public void costsWithWalls02() {
        //mix of multiple choice and single choice. 
        //Not as thorough, as wall distances are tested in the precomputed distance tests
        var width = 20; 
        var height = 10;
        var walls = new boolean[height][width];
        walls[0][10] = true;
        walls[1][10] = true;
        walls[2][10] = true;
        walls[3][10] = true;
        walls[4][10] = true;
        walls[5][10] = true;
        walls[6][10] = true;
        walls[7][10] = true;
        walls[8][10] = true;
        walls[9][10] = true;

        test1(width, height, walls);
        test2(width, height, walls);
        test3(width, height, walls);
        test4(width, height, walls);
        test5(width, height, walls);
        test6(width, height, walls);
        test7(width, height, walls);
        test8(width, height, walls);
    }
}
