package dtu.aimas.search.solvers.agent;

import dtu.aimas.common.*;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.Stopwatch;
import dtu.aimas.errors.SolutionNotFound;
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

import java.util.*;

public class WalledFinishedBoxes implements Solver {

    private final Solver subSolver;

    public WalledFinishedBoxes(Solver subSolver){
        this.subSolver = subSolver;
    }

    public WalledFinishedBoxes(){this(new AStar(new DistanceSumCost()));}

    @Override
    public Result<Solution> solve(Problem initial) {
        return ProblemParser.parse(initial).flatMap(this::solve);
    }

    private Result<Solution> solve(StateSpace space) {
        var fullProblem = space.getProblem();
        var initial = space.getInitialState();
        assert initial.agents.size() == 1 : "Single agent solver";
        assert fullProblem.agentGoals.size() <= 1 : "Single agent solver";

        var agent = initial.agents.iterator().next();

        ArrayList<StateSolution> solutions;

        IO.debug("solving boxes...");
        var boxGoals = new ArrayDeque<>(fullProblem.boxGoals);

        while(true){
            var solutionGoalPair = solveBoxes(boxGoals, fullProblem, initial);
            var solutionsResult = solutionGoalPair.solutions();
            if(solutionsResult.isOk()) {
                solutions = solutionsResult.get();
                break;
            }

            var unsolvableBoxGoal = solutionGoalPair.goal();
            boxGoals.remove(unsolvableBoxGoal);
            boxGoals.addFirst(unsolvableBoxGoal);
        }


//        // all boxes should now be solved
//
//        IO.debug("solving agent...");
//        for(var agentGoal: fullProblem.agentGoals){
//            subGoal[agentGoal.destination.row][agentGoal.destination.col] = agentGoal.label;
//            var iterativeProblem = fullProblem.copyWith(agents, boxes, subGoal);
//
//            // solve partial solution
//            var start = Stopwatch.getTimeMs();
//            var solutionResult = subSolver.solve(iterativeProblem).map(s -> (StateSolution)s);
//            IO.debug("agent solve time: %d ms", Stopwatch.getTimeSinceMs(start));
//            if(solutionResult.isError()) return Result.passError(solutionResult);
//
//            // save solution and iterate on next sub goal
//            var solution = solutionResult.get();
//            solutions.add(solution);
//        }

        // revert color changes
        resetBoxColors(solutions, agent.color);


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

    private void resetBoxColors(ArrayList<StateSolution> solutions, Color color) {
        for(var solution: solutions){
            for(var i = 0; i < solution.size(); i++){
                var state = solution.getState(i);
                for(var box : state.boxes){
                    if(box.color == Color.Mishmash){
                        box.color = color;
                    }
                }
            }
        }
    }

    private SolutionGoalPair solveBoxes(Collection<Goal> boxGoals, Problem fullProblem, State initial) {
        var agents = new ArrayList<>(initial.agents);
        var boxes = new ArrayList<>(initial.boxes);

        IO.debug("solving boxes...");
        var subGoal = new char[fullProblem.goals.length][fullProblem.goals[0].length];

        var walls = new boolean[fullProblem.walls.length][fullProblem.walls[0].length];
        for(var i = 0; i < walls.length; i++){
            for(var j = 0; j < walls[0].length; j++){
                walls[i][j] = fullProblem.walls[i][j];
            }
        }

        var solutions = new ArrayList<StateSolution>();
        for(var boxGoal: boxGoals){
            // add next box goal
            subGoal[boxGoal.destination.row][boxGoal.destination.col] = boxGoal.label;
            var iterativeProblem = fullProblem.copyWith(agents, boxes, subGoal, walls);
            IO.debug("#box goals: %d", iterativeProblem.boxGoals.size());
            IO.debug("problem:\n" + iterativeProblem.toString());

            // check if box is reachable for the new goal
            var solvable = isSolvable(iterativeProblem, agents, boxes, boxGoal);
            if(!solvable) return new SolutionGoalPair(Result.empty(), boxGoal);

            // solve partial solution
            var start = Stopwatch.getTimeMs();
            var solutionResult = subSolver.solve(iterativeProblem).map(s -> (StateSolution)s);
            IO.debug("solve time: %d ms", Stopwatch.getTimeSinceMs(start));
            if(solutionResult.isError()) {
                return new SolutionGoalPair(Result.passError(solutionResult), boxGoal);
            }

            // save solution and iterate on next sub goal
            var solution = solutionResult.get();
            solutions.add(solution);

            // update agents and boxes to where they left off from previous solution
            var finalState = solution.getState(solution.size()-1);
            agents = new ArrayList<>(finalState.agents);
            boxes = new ArrayList<>(finalState.boxes);

            // make finished box into a wall
            for(var box: boxes){
                if(!box.pos.equals(boxGoal.destination)) continue;
                box.color = Color.Mishmash;
                break;
            }

            walls[boxGoal.destination.row][boxGoal.destination.col] = true;
            subGoal[boxGoal.destination.row][boxGoal.destination.col] = 0;
        }

        return new SolutionGoalPair(Result.ok(solutions), null);
    }

    private static boolean isSolvable(Problem problem, ArrayList<Agent> agents, ArrayList<Box> boxes, Goal boxGoal) {
        for(var agent: agents){
            for (var box: boxes){
                if(box.color != agent.color) continue;
                if(box.label != boxGoal.label) continue;
                if(problem.admissibleDist(agent.pos, box.pos) == problem.MAX_DISTANCE) continue;
                if(problem.admissibleDist(box.pos, boxGoal.destination) == problem.MAX_DISTANCE) continue;
                return true;
            }
        }

        return false;
    }
}

record SolutionGoalPair(Result<ArrayList<StateSolution>> solutions, Goal goal){}
