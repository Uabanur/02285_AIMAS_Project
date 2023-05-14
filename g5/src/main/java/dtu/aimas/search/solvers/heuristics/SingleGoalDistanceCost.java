package dtu.aimas.search.solvers.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import dtu.aimas.common.Box;
import dtu.aimas.common.Position;
import dtu.aimas.communication.IO;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class SingleGoalDistanceCost implements Cost {
    @Override
    public int calculate(State state, StateSpace space) {
        var problem = space.problem();
        var result = 0;

        for(var goal : problem.boxGoals){
            if(state.boxes.stream().anyMatch(b -> b.label == goal.label && b.pos.equals(goal.destination)))
                continue;
            var compatibleBoxes = state.boxes.stream().filter(b -> b.label == goal.label).toList();
            var closestBox = compatibleBoxes.get(0);
            var minDist = Integer.MAX_VALUE;
            for(Box box : compatibleBoxes) {
                var dist = problem.admissibleDist(box.pos, goal.destination);
                if(dist < minDist) {
                    closestBox = box;
                    minDist = dist;
                }
            }
            result += minDist;
            //focus on keeping boxes in goals
            if(minDist > 0) {
                Box b = closestBox;
                var agent = state.agents.stream().filter(a -> a.color == b.color).findFirst().get();
                result += problem.admissibleDist(agent.pos, closestBox.pos);
            }
            
        }
        //if(result < 2)IO.info(result);
        //if(result > 0) result += 10; //penalty to keep agent from going to his goal

        for(var goal: problem.agentGoals){
            var agent = state.agents.stream().filter(a -> a.label == goal.label).findAny();
            if(agent.isEmpty()) throw new IllegalStateException("Unsatisfiable goal");
            result += problem.admissibleDist(agent.get().pos, goal.destination);
        }

        return result;
    }

    private int actualDist(Problem problem, State s, Position from, Position to) {
        List<Position> goals = List.of(to);
        if(goals.contains(from)) return 0;       
        HashMap<Position,Integer> distanceTo = new HashMap<>();
        LinkedList<Position> neighbors = new LinkedList<>();
        HashSet<Position> visited = new HashSet<>();
        neighbors.add(from);
        visited.add(from);
        distanceTo.put(from,0);
        while (!neighbors.isEmpty()) {
            Position current = neighbors.remove();
            if (goals.contains(current)) return distanceTo.get(current);
            int row = current.row;
            int col = current.col;
            Position top = new Position(row-1,col);
            if (row > 0 && !visited.contains(top) && !problem.walls[top.row][top.col]) {
                visited.add(top);
                int weight = getWeightPosition(top, s);
                distanceTo.put(top,distanceTo.get(current)+weight);
                neighbors.add(top);
            }
            Position left = new Position(row,col-1);
            if (col > 0 && !visited.contains(left) && !problem.walls[left.row][left.col]) {
                visited.add(left);
                int weight = getWeightPosition(left, s);
                distanceTo.put(left,distanceTo.get(current)+weight);
                neighbors.add(left);
            }
            Position bot = new Position(row+1,col);
            if (row < problem.walls.length - 1 && !visited.contains(bot) && !problem.walls[bot.row][bot.col]) {
                visited.add(bot);
                int weight = getWeightPosition(bot, s);
                distanceTo.put(bot,distanceTo.get(current)+weight);
                neighbors.add(bot);
            }
            Position right = new Position(row,col+1);
            if (col < problem.walls[row].length - 1 && !visited.contains(right) && !problem.walls[right.row][right.col]) {
                visited.add(right);
                int weight = getWeightPosition(right, s);
                distanceTo.put(right,distanceTo.get(current)+weight);
                neighbors.add(right);
            }
        }
        return 9999;
    }

    private static int getWeightPosition(Position pos, State s) {
        final int BOX_WEIGHT = 10; //could be influenced by map size (to account for time to move box out of blocked place)
        if (s.boxes.stream().anyMatch(b -> b.pos.equals(pos))) return BOX_WEIGHT;
        return 1;
    }
}
