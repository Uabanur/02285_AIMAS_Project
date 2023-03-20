package dtu.aimas.search;

import java.util.Collection;
import java.util.stream.Collectors;
import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Goal;

public class Problem {

    public Collection<Agent> agents;
    public Collection<Box> boxes;
    public boolean[][] walls;
    public char[][]goals;
    public Collection<Goal> agentGoals;
    public Collection<Goal> boxGoals;
    public int expectedStateSize;

    public Problem(Collection<Agent> agentCollection, Collection<Box> boxCollection, boolean[][] walls, char[][] goals, Collection<Goal> agentGoals, Collection<Goal> boxGoals) 
    {
        this.agents = agentCollection;
        this.boxes = boxCollection;
        this.walls = walls;
        this.goals = goals;
        this.agentGoals = agentGoals;
        this.boxGoals = boxGoals;
        expectedStateSize = 2<<15;
    }

    public Problem precompute(){
        return this;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        var commaSeparate = Collectors.joining(", ");
        var newline = System.lineSeparator();

        sb.append("Agents: ");
        sb.append(agents.stream().map(x -> x.toSimpleString()).collect(commaSeparate));
        sb.append(newline);

        sb.append("Boxes: ");
        sb.append(boxes.stream().map(x -> x.toSimpleString()).collect(commaSeparate));
        sb.append(newline);

        sb.append("Agents goals: ");
        sb.append(agentGoals.stream().map(x -> x.toSimpleString()).collect(commaSeparate));
        sb.append(newline);

        sb.append("Box goals: ");
        sb.append(boxGoals.stream().map(x -> x.toSimpleString()).collect(commaSeparate));
        sb.append(newline);

        sb.append("Walls and Goals:").append(newline);
        for(var row = 0; row < walls.length; row++){
            for(var col = 0; col < walls[row].length; col++){
                sb.append(
                    walls[row][col] ? "+" :
                    goals[row][col] > 0 ? String.valueOf(goals[row][col]) : 
                    " "
                );
            }
            if(row < walls.length - 1) sb.append(newline);
        }

        return sb.toString();
    }
}
