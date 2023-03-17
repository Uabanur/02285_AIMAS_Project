package dtu.aimas.search.solvers.heuristics;

import dtu.aimas.common.Box;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class GoalCount implements Cost {
    public int calculate(State state, StateSpace space) {
        // TODO : Update with changed state
        var problem = space.getProblem();
        return agentGoalCount(state, space, problem) + boxGoalCount(state, space, problem);
    }

    private int agentGoalCount(State state, StateSpace space, Problem problem) {
        var result = 0;
        for(var i = 0; i < problem.agents.size(); i++)
        {
            var symbol = (char) ('0' + i);
            var agentResult = space.getAgentByNumber(state, i);
            if(!agentResult.isPresent()) continue;
            
            var agent = agentResult.get();
            if (problem.goals[agent.pos.row][agent.pos.col] != symbol) {
                result += 1;
            }
        }
        return result;
    }

    private int boxGoalCount(State state, StateSpace space, Problem problem) {
        var result = 0;
        for(var row = 0; row < problem.goals.length; row ++){
            for(var col = 0; col < problem.goals[row].length; col ++){
                var symbol = problem.goals[row][col];
                if(!Box.isLabel(symbol)) continue;
                var boxResult = space.getBoxAt(state, row, col);
                if(!boxResult.isPresent()) continue;

                var box = boxResult.get();
                if (box.type != symbol) {
                    result += 1;
                }
            }
        }
        return result;
    }
}
