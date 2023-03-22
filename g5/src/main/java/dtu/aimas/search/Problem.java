package dtu.aimas.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.Queue;
import java.util.LinkedList;
import dtu.aimas.common.Position;
import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Goal;

public class Problem {

    public Collection<Agent> agents;
    public Collection<Box> boxes;
    public boolean[][] walls;
    public char[][] goals;
    public Collection<Goal> agentGoals;
    public Collection<Goal> boxGoals;
    public int expectedStateSize;
    private int[][][][] distances;

    public Problem(Collection<Agent> agentCollection, Collection<Box> boxCollection, boolean[][] walls, char[][] goals) 
    {
        this.agents = agentCollection;
        this.boxes = boxCollection;
        this.walls = walls;
        this.goals = goals;
        expectedStateSize = 2<<15;
      
        this.agentGoals = new ArrayList<Goal>();
        this.boxGoals = new ArrayList<Goal>();
        for(var row = 0; row < goals.length; row++){
            for(var col = 0; col < goals[row].length; col++){
                var c = goals[row][col];
                if(Agent.isLabel(c)){
                    this.agentGoals.add(new Goal(c, new Position(row, col)));
                }
                else if(Box.isLabel(c)){
                    this.boxGoals.add(new Goal(c, new Position(row, col)));
                }
            }
        }
        
      
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
        distances[src.row][src.col][src.row][src.col] = 0;
        neighbors.add(src);
        while(!neighbors.isEmpty()) {
            Position curr = neighbors.remove();
            int distToCurr = distances[src.row][src.col][curr.row][curr.col];
            
            Position top = new Position(curr.row-1,curr.col);
            if(top.row >= 0 && !walls[top.row][top.col] 
                    && distances[src.row][src.col][top.row][top.col] > distToCurr+1) {
                distances[src.row][src.col][top.row][top.col] = distToCurr+1;
                neighbors.add(top);             
            }
            Position left = new Position(curr.row,curr.col-1);
            if (left.col >= 0 && !walls[left.row][left.col] 
                    && distances[src.row][src.col][left.row][left.col] > distToCurr+1 ) {
                distances[src.row][src.col][left.row][left.col] = distToCurr+1;
                neighbors.add(left);
            }
            Position bot = new Position(curr.row+1,curr.col);
            if (bot.row < walls.length && !walls[bot.row][bot.col] 
                    && distances[src.row][src.col][bot.row][bot.col] > distToCurr+1) {
                distances[src.row][src.col][bot.row][bot.col] = distToCurr+1;
                neighbors.add(bot);
            }
            Position right = new Position(curr.row,curr.col+1);
            if (right.col < walls[right.row].length && !walls[right.row][right.col] 
                    && distances[src.row][src.col][right.row][right.col] > distToCurr+1) {
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

    public int admissibleDist(Position from, Position to) {
        return distances[from.row][from.col][to.row][to.col];
    }
}
