package dtu.aimas.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import dtu.aimas.common.Position;
import dtu.aimas.communication.IO;
import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;

public class Problem {

    public Collection<Agent> agents;
    public Collection<Box> boxes;
    public boolean[][] walls;
    public char[][] goals;
    private int[][][][] distances;

    public Problem(Collection<Agent> agentCollection, Collection<Box> boxCollection, boolean[][] walls, char[][] goals) 
    {
        this.agents = agentCollection;
        this.boxes = boxCollection;
        this.walls = walls;
        this.goals = goals;
        this.distances = new int[walls.length][walls[0].length][walls.length][walls[0].length];
        for(int i = 0; i < distances.length; i++) {
            for(int j = 0; j < distances[i].length; j++) {
                for(int l = 0; l < distances[i][j].length; l++) {
                    Arrays.fill(distances[i][j][l], Integer.MAX_VALUE);
                }
            }
        }
    }

    private void distanceFromPos(Position src) {
        Queue<Position> neighbors = new LinkedList<Position>();
        Set<Position> visited = new HashSet<Position>();
        visited.add(src);
        distances[src.row][src.col][src.row][src.col] = 0;
        neighbors.add(src);
        while(!neighbors.isEmpty()) {
            Position curr = neighbors.remove();
            int distToCurr = distances[src.row][src.col][curr.row][curr.col];
            
            Position top = new Position(curr.row-1,curr.col);
            if(top.row >= 0 && !visited.contains(top) && !walls[top.row][top.col]) {
                visited.add(top);
                distances[src.row][src.col][top.row][top.col] = distToCurr+1;
                neighbors.add(top);             
            }
            Position left = new Position(curr.row,curr.col-1);
            if (left.col >= 0 && !visited.contains(left) && !walls[left.row][left.col]) {
                visited.add(left);
                distances[src.row][src.col][left.row][left.col] = distToCurr+1;
                neighbors.add(left);
            }
            Position bot = new Position(curr.row+1,curr.col);
            if (bot.row < walls.length && !visited.contains(bot) && !walls[bot.row][bot.col]) {
                visited.add(bot);
                distances[src.row][src.col][bot.row][bot.col] = distToCurr+1;
                neighbors.add(bot);
            }
            Position right = new Position(curr.row,curr.col+1);
            if (right.col < walls[right.row].length && !visited.contains(right) && !walls[right.row][right.col]) {
                visited.add(right);
                distances[src.row][src.col][right.row][right.col] = distToCurr+1;
                neighbors.add(right);
            }
        }
    }

    public Problem precompute(){
        for(int x = 0; x < walls.length; x++) {
            for(int y = 0; y < walls[x].length; y++) {
                distanceFromPos(new Position(x,y));
            }
        }
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

    public int admissibleDist(Position from, Position to) {
        return distances[from.row][from.col][to.row][to.col];
    }
}
