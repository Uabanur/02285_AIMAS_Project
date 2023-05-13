package dtu.aimas.search.solvers.heuristics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import dtu.aimas.common.Position;
import dtu.aimas.communication.IO;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class MixedDistanceSumCost implements Cost {
    @Override
    public int calculate(State state, StateSpace space) {
        var problem = space.problem();
        var result = 0;

        for(var goal : problem.boxGoals){
            var minGoalDistance = state.boxes.stream()
                    .filter(b -> b.label == goal.label)
                    .map(b -> problem.admissibleDist(b.pos, goal.destination))
                    .min(Integer::compareTo)
                    .orElse(0);
            //focus on keeping boxes in goals
            if(minGoalDistance > 0) minGoalDistance+=1;
            result += minGoalDistance;
        }
        //if(result < 2)IO.info(result);
        //if(result > 0) result += 10; //penalty to keep agent from going to his goal
        
        for(var agent : problem.agents) {
            var minAgentBoxDist = state.boxes.stream()
                    .filter(b -> b.color == agent.color && problem.goals[b.pos.row][b.pos.col] != b.label)
                    .map(b -> problem.admissibleDist(b.pos, agent.pos))
                    .min(Integer::compareTo)
                    .orElse(0);
            result += minAgentBoxDist/2;
        }

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
