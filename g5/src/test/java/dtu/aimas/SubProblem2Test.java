package dtu.aimas;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Color;
import dtu.aimas.common.Position;
import dtu.aimas.communication.IO;
import dtu.aimas.common.Goal;
import dtu.aimas.search.Problem;

public class SubProblem2Test {
    
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
        problem.assignGoals();

        var subProblem0 = problem.subProblemFor2(agents.get(0));
        var expectedAgents0 = new Agent[]{agents.get(0)};
        var expectedBoxes0 = new Box[]{boxes.get(0)};
        var expectedBoxGoals0 = new Goal[]{new Goal(expectedBoxes0[0].label, expectedBoxes0[0].pos)};
        Assert.assertArrayEquals(expectedAgents0, subProblem0.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes0, subProblem0.boxes.toArray(Box[]::new));
        Assert.assertArrayEquals(expectedBoxGoals0, subProblem0.boxGoals.toArray(Goal[]::new));
        Assert.assertArrayEquals(new Goal[]{}, subProblem0.agentGoals.toArray(Goal[]::new));
        
        var subProblem1 = problem.subProblemFor2(agents.get(1));
        agents.get(1).label = '0';
        var expectedAgents1 = new Agent[]{agents.get(1)};
        var expectedBoxes1 = new Box[]{boxes.get(1)};
        var expectedBoxGoals1 = new Goal[]{new Goal(expectedBoxes1[0].label, expectedBoxes1[0].pos)};
        Assert.assertArrayEquals(expectedAgents1, subProblem1.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes1, subProblem1.boxes.toArray(Box[]::new));
        Assert.assertArrayEquals(expectedBoxGoals1, subProblem1.boxGoals.toArray(Goal[]::new));
        Assert.assertArrayEquals(new Goal[]{}, subProblem1.agentGoals.toArray(Goal[]::new));   
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
        problem.assignGoals();

        var subProblem0 = problem.subProblemFor2(agents.get(0));
        var expectedAgents0 = new Agent[]{agents.get(0)};
        var expectedBoxes0 = new Box[]{boxes.get(0)};
        var expectedBoxGoals0 = problem.boxGoals.stream().filter(
            bg -> bg.label == expectedBoxes0[0].label && bg.destination.equals(expectedBoxes0[0].pos)
            ).toArray();
        Assert.assertArrayEquals(expectedAgents0, subProblem0.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes0, subProblem0.boxes.toArray(Box[]::new));
        Assert.assertArrayEquals(expectedBoxGoals0, subProblem0.boxGoals.toArray(Goal[]::new));
        Assert.assertArrayEquals(new Goal[]{}, subProblem0.agentGoals.toArray(Goal[]::new));
        
        var subProblem1 = problem.subProblemFor2(agents.get(1));
        agents.get(1).label = '0';
        var expectedAgents1 = new Agent[]{agents.get(1)};
        var expectedBoxes1 = new Box[]{boxes.get(1)};
        var expectedBoxGoals1 = problem.boxGoals.stream().filter(
            bg -> bg.label == expectedBoxes1[0].label && bg.destination.equals(expectedBoxes1[0].pos)
            ).toArray();
        Assert.assertArrayEquals(expectedAgents1, subProblem1.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes1, subProblem1.boxes.toArray(Box[]::new));
        Assert.assertArrayEquals(expectedBoxGoals1, subProblem1.boxGoals.toArray(Goal[]::new));
        Assert.assertArrayEquals(new Goal[]{}, subProblem1.agentGoals.toArray(Goal[]::new));
    }

    @Test
    public void SubProblem_Of_TwoAgents_Same_Color_Harder_Goals(){
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
        
        goals[5][5] = agents.get(0).label;
        goals[9][0] = agents.get(1).label;
        goals[0][9] = boxes.get(0).label;
        goals[9][9] = boxes.get(1).label;

        var problem = new Problem(agents, boxes, walls, goals);
        problem.assignGoals();

        var subProblem0 = problem.subProblemFor2(agents.get(0));
        var expectedAgents0 = new Agent[]{agents.get(0)};
        var expectedBoxes0 = new Box[]{boxes.get(0)};
        var expectedBoxGoals0 = new Goal[]{new Goal(expectedBoxes0[0].label, new Position(0,9))};
        Assert.assertArrayEquals(expectedAgents0, subProblem0.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes0, subProblem0.boxes.toArray(Box[]::new));
        Assert.assertArrayEquals(expectedBoxGoals0, subProblem0.boxGoals.toArray(Goal[]::new));
        Assert.assertArrayEquals(new Goal[]{}, subProblem0.agentGoals.toArray(Goal[]::new));
        
        var subProblem1 = problem.subProblemFor2(agents.get(1));
        agents.get(1).label = '0';
        var expectedAgents1 = new Agent[]{agents.get(1)};
        var expectedBoxes1 = new Box[]{boxes.get(1)};
        var expectedBoxGoals1 = new Goal[]{new Goal(expectedBoxes1[0].label, new Position(9,9))};
        Assert.assertArrayEquals(expectedAgents1, subProblem1.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes1, subProblem1.boxes.toArray(Box[]::new));
        Assert.assertArrayEquals(expectedBoxGoals1, subProblem1.boxGoals.toArray(Goal[]::new));
        Assert.assertArrayEquals(new Goal[]{}, subProblem1.agentGoals.toArray(Goal[]::new));
    }

    @Test
    public void SubProblem_Of_Agent_Without_Available_Boxes(){
        var height = 10;
        var width = 10;

        var agents = List.of(
            new Agent(new Position(1, 1), Color.Red, '0')
        );

        var boxes = List.of(
            new Box(new Position(1, 2), Color.Blue, 'A')
        );

        var walls = new boolean[width][height];
        var goals = new char[width][height];
        
        goals[5][5] = agents.get(0).label;
        goals[0][9] = boxes.get(0).label;

        var problem = new Problem(agents, boxes, walls, goals);
        problem.assignGoals();

        var subProblem0 = problem.subProblemFor2(agents.get(0));
        var expectedAgents0 = new Agent[]{agents.get(0)};
        var expectedBoxes0 = new Box[]{};
        var expectedBoxGoals0 = new Goal[]{};
        var expectedAgentGoals0 = problem.agentGoals.stream().filter(
            bg -> bg.label == expectedAgents0[0].label
            ).toArray();
        Assert.assertArrayEquals(expectedAgents0, subProblem0.agents.toArray(Agent[]::new));
        Assert.assertArrayEquals(expectedBoxes0, subProblem0.boxes.toArray(Box[]::new));
        Assert.assertArrayEquals(expectedBoxGoals0, subProblem0.boxGoals.toArray(Goal[]::new));
        Assert.assertArrayEquals(expectedAgentGoals0, subProblem0.agentGoals.toArray(Goal[]::new));
    }
    
}
