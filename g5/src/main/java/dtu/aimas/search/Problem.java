package dtu.aimas.search;

import dtu.aimas.common.*;
import dtu.aimas.search.solvers.graphsearch.State;

import java.util.Map;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Problem {
    public final int MAX_DISTANCE = Integer.MAX_VALUE;
    public final Collection<Agent> agents;
    public final Collection<Box> boxes;
    public final boolean[][] walls;
    public final char[][] goals;
    public final Collection<Goal> agentGoals;
    public final Collection<Goal> boxGoals;
    public final int expectedStateSize;
    private int[][][][] distances;

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
    }

    // todo these should be private and only called by `copyWith` method which can be overwritten by
    // ConstrainedProblem and SafeProblem, such that sub problem contain the parent problem restrictions.
    public Problem(Collection<Agent> agents, Collection<Box> boxes, char[][] goals, Problem parent) {
        this(agents, boxes, goals, parent.walls, parent);
    }

    private Problem(Collection<Agent> agents, Collection<Box> boxes, char[][] goals, boolean[][] walls, Problem parent) {
        this.agents = agents;
        this.boxes = boxes;
        this.goals = goals;
        this.agentGoals = extractGoals(Agent::isLabel);
        this.boxGoals = extractGoals(Box::isLabel);

        this.walls = walls;
        this.expectedStateSize = parent.expectedStateSize;
        this.distances = parent.distances;
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
                .forEach(a -> Arrays.fill(a, MAX_DISTANCE));
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
            if(distToCurr == MAX_DISTANCE) continue;

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

    public boolean validGoalState(State state) {
        return true;
    }

    public Problem copyWith(List<Agent> agents, List<Box> boxes, char[][] goals) {
        return new Problem(agents, boxes, goals, this);
    }

    public Problem copyWith(List<Agent> agents, List<Box> boxes, char[][] goals, boolean[][] walls) {
        return new Problem(agents, boxes, walls, goals).precompute();
    }

}
