package dtu.aimas.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dtu.aimas.common.Position;
import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Color;
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
    private Goal[] agentAssignedGoal;
    private Box[] agentAssignedBox;

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
        var height = walls.length;
        var width = walls[0].length;

        var init = new char[height][width];
        var goal = new char[height][width];
        for(var row = 0; row < height; row++){
            for(var col = 0; col < width; col++){
                var symbol = walls[row][col] ? '+' : ' ';
                init[row][col] = symbol;
                goal[row][col] = symbol;
            }
        }

        for(var agent: agents){
            init[agent.pos.row][agent.pos.col] = agent.label;
        }

        for(var box: boxes){
            init[box.pos.row][box.pos.col] = box.label;
        }

        for(var g: agentGoals){
            goal[g.destination.row][g.destination.col] = g.label;
        }
        for(var g: boxGoals){
            goal[g.destination.row][g.destination.col] = g.label;
        }


        var sb = new StringBuilder();
        var commaSeparate = Collectors.joining(", ");
        var newline = System.lineSeparator();

        sb.append("#colors:").append(newline);

        Map<Color, List<Agent>> agentColors = agents.stream() .collect(Collectors.groupingBy(a -> a.color));
        Map<Color, List<Box>> boxColors = boxes.stream() .collect(Collectors.groupingBy(b -> b.color));

        for(var color :Color.values()){
            if (!agentColors.containsKey(color) && !boxColors.containsKey(color)) continue;
            sb.append(color.name());
            sb.append(": ");

            for(var agent : agentColors.getOrDefault(color, List.of())) 
                sb.append(agent.label).append(", ");

            for(var box : boxColors.getOrDefault(color, List.of())) 
                sb.append(box.label).append(", ");

            sb.setLength(sb.length()-2); // trim last comma
            sb.append(newline);
        }

        sb.append("#initial").append(newline);
        for(var row = 0; row < height; row++ ){
            for(var col = 0; col < width; col++ ){
                sb.append(init[row][col]);
            }
            sb.append(newline);
        }
        sb.append("#goal").append(newline);
        for(var row = 0; row < height; row++ ){
            for(var col = 0; col < width; col++ ){
                sb.append(goal[row][col]);
            }
            if(row < width-1) sb.append(newline);
        }

        return sb.toString();
    }

    public Problem subProblemFor(Agent agent) {
        // Should only contain the agents given, and their boxes.
        // Walls are the same, and goals only contain goals for the agent and its boxes.
        
        // in order to create a subproblem and stay within domain rules we have to reorder the agents naming
        var subAgent = new Agent(agent.pos, agent.color, '0');
        var boxes = this.boxes.stream().filter(b -> b.color == agent.color).collect(Collectors.toList());

        // Deep copy of the goals array
        char[][] goals = new char[this.goals.length][this.goals[0].length];
        for (int i = 0; i < this.goals.length; i++) {
            goals[i] = Arrays.copyOf(this.goals[i], this.goals[i].length);
        }

        for(var row = 0; row < goals.length; row++){
            for(var col = 0; col < goals[row].length; col++){
                var symbol = goals[row][col];
                if(boxes.stream().anyMatch(b -> b.label == symbol)){
                    // boxes can stay like this
                    continue;
                }
                if(symbol == agent.label){
                    // agent goal has to be renamed
                    goals[row][col] = subAgent.label;
                } 
                else{
                    goals[row][col] = '\0';
                }
            }
        }
        
        return new Problem(List.of(subAgent), boxes, walls, goals);
    }

    public void assignGoals() {
        //this can be used for the initial subproblem generation
        agentAssignedBox = new Box[agents.size()];
        agentAssignedGoal = new Goal[agents.size()];
        Set<Box> assignedBoxes = new HashSet<>();
        
        //simple assignation to start off, it's dependent on boxGoal ordering
        for(Goal goal : boxGoals) {
            List<Box> compatibleBoxes = this.boxes.stream().filter(b -> b.label == goal.label && !assignedBoxes.contains(b)).collect(Collectors.toList());
            if(compatibleBoxes.size() == 0) continue;
            Box closestBox = compatibleBoxes.get(0);
            int closestBoxDist = Integer.MAX_VALUE;
            for(Box box : compatibleBoxes) {
                int dist = admissibleDist(box.pos, goal.destination);
                if(dist < closestBoxDist) {
                    closestBox = box;
                    closestBoxDist = dist;
                }
            }
            //idk why this needs to be done, but otherwise there is an error
            Box theBox = closestBox;
            List<Agent> compatibleAgents = this.agents.stream().filter(
                a -> a.color == theBox.color && agentAssignedGoal[Character.getNumericValue(a.label)] == null
            ).collect(Collectors.toList());
            if(compatibleAgents.size() == 0) continue;
            Agent closestAgent = compatibleAgents.get(0);
            int closestAgentDist = Integer.MAX_VALUE;
            for(Agent agent : compatibleAgents) {
                int dist = admissibleDist(agent.pos, closestBox.pos);
                if(dist < closestAgentDist) {
                    closestAgent = agent;
                    closestAgentDist = dist;
                }
            }
            int agentIndex = Character.getNumericValue(closestAgent.label);
            agentAssignedBox[agentIndex] = closestBox;
            agentAssignedGoal[agentIndex] = goal;
            assignedBoxes.add(closestBox);
        }
    }

    public int admissibleDist(Position from, Position to) {
        return distances[from.row][from.col][to.row][to.col];
    }

    public boolean isFree(Position pos, Agent agent, int timeStep) {
        return !walls[pos.row][pos.col];
    }
}
