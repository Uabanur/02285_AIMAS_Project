package dtu.aimas.search;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;

public class Problem {

    public Collection<Agent> agents;
    public Collection<Box> boxes;
    public boolean[][] walls;
    public char[][]goals;

    public Problem(Collection<Agent> agentCollection, Collection<Box> boxCollection, boolean[][] walls, char[][] goals) 
    {
        this.agents = agentCollection;
        this.boxes = boxCollection;
        this.walls = walls;
        this.goals = goals;
    }

    public Problem precompute(){
        return this;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("Agents: ");
        sb.append(agents.stream()
            .map(a -> ("(" + a.pos.row + "," + a.pos.col + "|" + a.color.name() + ")"))
            .collect(Collectors.joining(", "))
        ).append(System.lineSeparator());

        sb.append("Boxes: ");
        sb.append(boxes.stream()
            .map(b -> ("(" + b.pos.row + "," + b.pos.col + ":" + b.type + "|" + b.color.name() + ")"))
            .collect(Collectors.joining(", "))
        ).append(System.lineSeparator());

        sb.append("Walls and Goals:").append(System.lineSeparator());
        sb.append(
            IntStream.range(0, walls.length)
            .mapToObj(row ->
                IntStream.range(0, walls[row].length)
                .mapToObj(col -> 
                    walls[row][col] ? "+"
                    : goals[row][col] > 0 ? String.valueOf(goals[row][col]) 
                    : " "
                ).collect(Collectors.joining())
            ).collect(Collectors.joining(System.lineSeparator())));

        return sb.toString();
    }
}
