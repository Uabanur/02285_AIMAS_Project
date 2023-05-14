package dtu.aimas.search.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;

import dtu.aimas.common.Goal;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;

public class SAOrderedSolver implements Solver {
    private Solver subSolver;
    
    public SAOrderedSolver(Solver subSolver){
        this.subSolver = subSolver;
    }
    
    public Result<Solution> solve(Problem initial) {
        IO.debug("Solving goals one at a time");
        int height = initial.goals.length;
        int width = initial.goals[0].length;
        ArrayList<StateSolution> solutions = new ArrayList<>();
        char goals[][] = new char[height][width];
        var agents = initial.agents;
        var boxes = initial.boxes;
        var boxGoals = orderedGoalsByPriority(initial);
        for(var goal : boxGoals) {
            IO.debug("Solving goal %c",goal.label);
            //add one goal at a time
            goals[goal.destination.row][goal.destination.col] = goal.label;
            Problem subProblem = initial.copyWith(agents.stream().toList(), boxes.stream().toList(), goals);
            var subSol = subSolver.solve(subProblem);
            if(subSol.isOk()) {
                var sol = (StateSolution)subSol.get();
                solutions.add(sol);
                //get the position of agent in last step
                agents = sol.getState(sol.size()-1).agents;
                boxes = sol.getState(sol.size()-1).boxes;
            }
            else {
                return Result.error(subSol.getError());
            }
        }
        for(var goal : initial.agentGoals) {
            IO.debug("Solving agent goal %c",goal.label);
            goals[goal.destination.row][goal.destination.col] = goal.label;
            Problem subProblem = initial.copyWith(agents.stream().toList(), boxes.stream().toList(), goals);
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

    private List<Goal> orderedGoalsByPriority(Problem problem) {
        List<Goal> orderedBoxGoals = new ArrayList<Goal>();
        //We want goals in dead-ends to be solved first and those in chokepoints last
        for(Goal goal : problem.boxGoals) {
            if(problem.isDeadEnd(goal.destination)) {
                orderedBoxGoals.add(0, goal);
            }
            else if(problem.isChokepoint(goal.destination)) {
                orderedBoxGoals.add(Math.max(0,orderedBoxGoals.size()-1), goal);
            }
            else {
                int insertPosition = Math.ceilDiv((orderedBoxGoals.size()-1),2);
                orderedBoxGoals.add(Math.max(0,insertPosition), goal);
            }
        }
        return orderedBoxGoals;
    }
}
