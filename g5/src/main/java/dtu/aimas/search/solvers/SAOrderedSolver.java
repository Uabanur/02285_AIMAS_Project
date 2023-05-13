package dtu.aimas.search.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;

public class SAOrderedSolver implements Solver {
    private Solver subSolver;
    
    public SAOrderedSolver(Solver subSolver){
        this.subSolver = subSolver;
    }
    
    public Result<Solution> solve(Problem initial) {
        int height = initial.goals.length;
        int width = initial.goals[0].length;
        ArrayList<StateSolution> solutions = new ArrayList<>();
        char goals[][] = new char[height][width];
        var agents = initial.agents;
        for(var goal : initial.boxGoals) {
            //add one goal at a time
            goals[goal.destination.row][goal.destination.col] = goal.label;
            Problem subProblem = new Problem(agents, initial.boxes, goals, initial);
            var subSol = subSolver.solve(subProblem);
            if(subSol.isOk()) {
                var sol = (StateSolution)subSol.get();
                solutions.add(sol);
                //get the position of agent in last step
                agents = sol.getState(sol.size()-1).agents;
            }
            else {
                return Result.error(subSol.getError());
            }
        }
        for(var goal : initial.agentGoals) {
            goals[goal.destination.row][goal.destination.col] = goal.label;
            Problem subProblem = new Problem(agents, initial.boxes, goals, initial);
            var subSol = subSolver.solve(subProblem);
            if(subSol.isOk()) {
                solutions.add((StateSolution)subSol.get());
            }
            else {
                return Result.error(subSol.getError());
            }
        }
        return Result.ok(SolutionMerger.sequentialJoin(solutions));
    }
}
