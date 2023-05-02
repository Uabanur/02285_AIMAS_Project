package dtu.aimas.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    public final Collection<Agent> agents;
    public final Collection<Box> boxes;
    public final boolean[][] walls;
    public final char[][] goals;
    public final Collection<Goal> agentGoals;
    // TODO(6): could be final, but then the ordering have to be done in the constructor
    public Collection<Goal> boxGoals;
    public final int expectedStateSize;
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

        this.agentGoals = extractGoals(Agent::isLabel);
        this.boxGoals = extractGoals(Box::isLabel);
        this.distances = initializeDistances();

        this.agentAssignedBox = new Box[agents.size()];
        this.agentAssignedGoal = new Goal[agents.size()];
        
        // TODO(4): problem to investigate during follow conflict
        // orderGoalsByPriority();
    }

    public Problem(Collection<Agent> agents, Collection<Box> boxes, char[][] goals, Problem parent) {
        this.agents = agents;
        this.boxes = boxes;
        this.goals = goals;
        this.agentGoals = extractGoals(Agent::isLabel);
        this.boxGoals = extractGoals(Box::isLabel);

        this.walls = parent.walls;
        this.expectedStateSize = parent.expectedStateSize;
        this.distances = parent.distances;

        this.agentAssignedBox = new Box[agents.size()];
        this.agentAssignedGoal = new Goal[agents.size()];
        
        // TODO(4): problem to investigate during follow conflict
        // orderGoalsByPriority();
    }

    private Collection<Goal> extractGoals(Function<Character, Boolean> predicate){
        var result = new ArrayList<Goal>();
        for(var row = 0; row < goals.length; row++){
            for(var col = 0; col < goals[row].length; col++){
                var c = goals[row][col];
                if (!predicate.apply(c)) continue;

                result.add(new Goal(c, new Position(row, col)));
            }
        }
        return result;
    }

    private int[][][][] initializeDistances(){
        var distances = new int[walls.length][walls[0].length][walls.length][walls[0].length];
        Arrays.stream(distances)
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .forEach(a -> Arrays.fill(a, Integer.MAX_VALUE));
        return distances;
    }

    private void distanceFromPos(Position src) {
        if(walls[src.row][src.col]) return;
        Queue<Position> neighbors = new LinkedList<Position>();
        distances[src.row][src.col][src.row][src.col] = 0;
        neighbors.add(src);
        while(!neighbors.isEmpty()) {
            Position curr = neighbors.remove();
            int distToCurr = distances[src.row][src.col][curr.row][curr.col];
            if(distToCurr == Integer.MAX_VALUE) continue;

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

    public void orderGoalsByPriority() {
        ArrayList<Goal> newBoxGoals = new ArrayList<Goal>();
        //We want goals in dead-ends to be solved first and those in chokepoints last
        for(Goal goal : boxGoals) {
            if(isDeadEnd(goal.destination)) {
                newBoxGoals.add(0, goal);
            }
            else if(isChokepoint(goal.destination)) {
                newBoxGoals.add(newBoxGoals.size()-1, goal);
            }
            else {
                int insertPosition = Math.ceilDiv((newBoxGoals.size()-1),2);
                newBoxGoals.add(insertPosition, goal);
            }
        }

        this.boxGoals = newBoxGoals;
    }

    public Problem subProblemFor(Agent agent)
    {
        var agents = List.of(agent);
        var subBoxes = boxes.stream().filter(b -> 
            b.color == agent.color).collect(Collectors.toList());
        var subGoals = new char[goals.length][goals[0].length];

        for(var row = 0; row < goals.length; row++){
            for(var col = 0; col < goals[row].length; col++){
                var symbol = goals[row][col];
                if (symbol == 0) continue;
                if (symbol == agent.label){
                    subGoals[row][col] = symbol;
                }  else
                if (subBoxes.stream().anyMatch(b -> b.label == symbol)) {
                    subGoals[row][col] = symbol;
                }
            }
        }
        return new Problem(agents, subBoxes, walls, subGoals);
    }

    public Problem subProblemFor2(Agent agent) {
        var subAgent = new Agent(agent.pos, agent.color, '0');
        List<Box> boxes = new ArrayList<Box>();

        int agentNum = Character.getNumericValue(agent.label);
        char[][] goals = new char[this.goals.length][this.goals[0].length];
        //only leave the assigned goal
        Goal boxGoal = agentAssignedGoal[agentNum];
        if(boxGoal != null) {
            goals[boxGoal.destination.row][boxGoal.destination.col] = boxGoal.label;
        }
        else {
            //if no box goal assigned, try to see if a new one can be assigned
            assignGoals();
            boxGoal = agentAssignedGoal[agentNum];
            if(boxGoal != null) {
                goals[boxGoal.destination.row][boxGoal.destination.col] = boxGoal.label;
            }
            //otherwise assign agentGoal
            else {
                var agentGoalOption = agentGoals.stream().filter(agoal -> agoal.label == agent.label).findAny();
                if(agentGoalOption.isPresent()) {
                    Goal agentGoal = agentGoalOption.get();
                    goals[agentGoal.destination.row][agentGoal.destination.col] = subAgent.label;
                }
            }
        }

        Box assignedBox = agentAssignedBox[agentNum];
        if(assignedBox != null) {
            //if agent has a box assigned, only leave that one in the subproblem
            boxes.add(assignedBox);
        }

        return new Problem(List.of(subAgent), boxes, walls, goals);
    }

    public void assignGoals() {
        //this can be used for the initial subproblem generation
        Set<Box> assignedBoxes = Arrays.stream(agentAssignedBox).collect(Collectors.toSet());
        Set<Goal> assignedGoals = Arrays.stream(agentAssignedGoal).collect(Collectors.toSet());
        Collection<Agent> freeAgents = agents.stream().filter(a -> agentAssignedGoal[Character.getNumericValue(a.label)] == null).collect(Collectors.toList());
        
        //simple assignation to start off, it's dependent on boxGoal ordering
        for(Goal goal : boxGoals) {
            if(assignedGoals.contains(goal)) continue;
            
            List<Box> compatibleBoxes = this.boxes.stream().filter(
                b -> b.label == goal.label && !assignedBoxes.contains(b) 
                && freeAgents.stream().anyMatch(a -> a.color.equals(b.color))
                ).collect(Collectors.toList());
            if(compatibleBoxes.isEmpty()) continue;

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
            if(compatibleAgents.isEmpty()) continue;
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

    public boolean isChokepoint(Position pos) {
        int row = pos.row; int col = pos.col;
        boolean firstRow = row == 0;
        boolean lastRow = row == walls.length-1;
        boolean firstCol = col == 0;
        boolean lastCol = col == walls[0].length-1;
        if(isDeadEnd(pos)) return false;
        return (firstRow || walls[row-1][col]) && (lastRow || walls[row+1][col])
            || (firstCol || walls[row][col-1]) && (lastCol || walls[row][col+1]) 
            || (   (firstRow || ((firstCol || walls[row-1][col-1]) && (lastCol || walls[row-1][col+1])))
                && (lastRow  || ((firstCol || walls[row+1][col-1]) && (lastCol || walls[row+1][col+1]))));
    }

    public boolean isDeadEnd(Position pos) {
        int freeNeighbors = 0;
        if(pos.row > 0 && !walls[pos.row-1][pos.col]) freeNeighbors++;
        if(pos.row < walls.length-1 && !walls[pos.row+1][pos.col]) freeNeighbors++;
        if(pos.col > 0 && !walls[pos.row][pos.col-1]) freeNeighbors++;
        if(pos.col < walls[0].length-1 && !walls[pos.row][pos.col+1]) freeNeighbors++;
        return freeNeighbors < 2;
    }

}
