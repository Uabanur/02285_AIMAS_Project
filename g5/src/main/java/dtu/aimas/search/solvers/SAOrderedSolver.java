package dtu.aimas.search.solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;

import dtu.aimas.common.Goal;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.problems.ProblemSplitter;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.agent.WalledFinishedBoxes;

public class SAOrderedSolver implements Solver {
    private Solver subSolver;
    private ProblemSplitter splitter;
    
    public SAOrderedSolver(Solver subSolver){
        this.subSolver = subSolver;
        this.splitter = null;
    }

    public SAOrderedSolver(Solver subSolver, ProblemSplitter splitter) {
        this.subSolver = subSolver;
        this.splitter = splitter;
    }

    public Result<Solution> solve(Problem initial) {
        if(splitter != null) {
            var sols = splitter.split(initial).stream().map(this::solveSplit).map(r -> (StateSolution)r.get()).toList();
            return Result.ok(SolutionMerger.mergeSolutions(sols));
        }
        return solveSplit(initial);
    }
    
    private Result<Solution> solveSplit(Problem initial) {
        IO.debug("Solving goals one at a time");
        int height = initial.goals.length;
        int width = initial.goals[0].length;
        ArrayList<StateSolution> solutions = new ArrayList<>();
        char goals[][] = new char[height][width];
        var agents = initial.agents;
        var boxes = initial.boxes;
        //var boxGoals = orderedGoalsByPriority(initial);
        var boxGoals = WalledFinishedBoxes.getSolvablyOrderedBoxGoals(initial.boxGoals, new ArrayList<>(agents), new ArrayList<>(boxes), initial);
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
        if(solutions.isEmpty()) {
            //For problems without goals
            var emptySol = subSolver.solve(initial);
            if(emptySol.isOk()) {
                solutions.add((StateSolution)emptySol.get());
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
