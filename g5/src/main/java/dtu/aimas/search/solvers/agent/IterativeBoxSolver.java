package dtu.aimas.search.solvers.agent;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.Stopwatch;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.SolutionChecker;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;

import java.util.ArrayList;

public class IterativeBoxSolver implements Solver {

    private final Solver subSolver;

    public IterativeBoxSolver(Solver subSolver){
        this.subSolver = subSolver;
    }

    public IterativeBoxSolver(){this(new AStar(new DistanceSumCost()));}

    @Override
    public Result<Solution> solve(Problem initial) {
        return ProblemParser.parse(initial).flatMap(this::solve);
    }

    private Result<Solution> solve(StateSpace space) {
        var fullProblem = space.getProblem();
        var initial = space.getInitialState();
        assert initial.agents.size() == 1 : "Single agent solver";
        assert fullProblem.agentGoals.size() <= 1 : "Single agent solver";

        var agents = initial.agents;
        var boxes = initial.boxes;
        var solutions = new ArrayList<StateSolution>();

        IO.debug("solving boxes...");
        var subGoal = new char[fullProblem.goals.length][fullProblem.goals[0].length];
        for(var boxGoal: fullProblem.boxGoals){
            // add next box goal
            subGoal[boxGoal.destination.row][boxGoal.destination.col] = boxGoal.label;
            var iterativeProblem = fullProblem.copyWith(agents, boxes, subGoal);

            IO.debug("#box goals: %d", iterativeProblem.boxGoals.size());

            // solve partial solution
            var start = Stopwatch.getTimeMs();
            var solutionResult = subSolver.solve(iterativeProblem).map(s -> (StateSolution)s);
            IO.debug("solve time: %d ms", Stopwatch.getTimeSinceMs(start));
            if(solutionResult.isError()) return Result.passError(solutionResult);

            // save solution and iterate on next sub goal
            var solution = solutionResult.get();
            solutions.add(solution);

            // update agents and boxes to where they left off from previous solution
            var finalState = solution.getState(solution.size()-1);
            agents = finalState.agents;
            boxes = finalState.boxes;
        }

        IO.debug("solving agent...");
        // all boxes should now be solved
        for(var agentGoal: fullProblem.agentGoals){
            subGoal[agentGoal.destination.row][agentGoal.destination.col] = agentGoal.label;
            var iterativeProblem = fullProblem.copyWith(agents, boxes, subGoal);

            // solve partial solution
            var start = Stopwatch.getTimeMs();
            var solutionResult = subSolver.solve(iterativeProblem).map(s -> (StateSolution)s);
            IO.debug("agent solve time: %d ms", Stopwatch.getTimeSinceMs(start));
            if(solutionResult.isError()) return Result.passError(solutionResult);

            // save solution and iterate on next sub goal
            var solution = solutionResult.get();
            solutions.add(solution);
        }

        // merge partial solutions
        IO.debug("Merging solutions...");
        var lastState = solutions.get(0).getLastState();
        for(var i = 1; i < solutions.size(); i++){
            var solution = solutions.get(i);
            var start = solution.getState(0);

            // solution start should be end of last state
            assert lastState.agents.equals(start.agents);
            for(var box: lastState.boxes)
                assert start.boxes.contains(box);

            // when merging the start state of the next solution is omitted
            for(var step = 1; step < solution.size(); step++){
                var state = solution.getState(step);

                // states are linked to update parent links and step counter
                lastState = new State(lastState, state.agents, state.boxes, state.jointAction);
            }
        }

        var mergedSolutionResult = space.createSolution(lastState).map(s -> (StateSolution)s);
        if(mergedSolutionResult.isError()) Result.passError(mergedSolutionResult);

        var mergedSolution = mergedSolutionResult.get();
        var valid = SolutionChecker.validSolution(mergedSolution, space);
        assert valid : "Combined solution should be valid";

        return Result.ok(mergedSolution);
    }
}
