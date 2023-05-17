package dtu.aimas.search.solvers.safeinterval;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.communication.Stopwatch;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.problems.ColorProblemSplitter;
import dtu.aimas.search.problems.ProblemSplitter;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solutions.StateSolution;
import dtu.aimas.search.solvers.SolutionChecker;
import dtu.aimas.search.solvers.SolutionMerger;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.blackboard.Attempt;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SafePathSolver implements Solver {
    private final Solver subSolver;
    private final ProblemSplitter splitter;
    private final int attemptThreshold;
    private final boolean allowMerges;

    public SafePathSolver(Solver subSolver, ProblemSplitter splitter, boolean allowMerges, int attemptThreshold){
        this.allowMerges = allowMerges;
        this.subSolver = subSolver;
        this.splitter = splitter;
        this.attemptThreshold = attemptThreshold;
    }
    public SafePathSolver(Solver subSolver, ProblemSplitter splitter, int attemptThreshold){this(subSolver, splitter, true, attemptThreshold);}
    public SafePathSolver(Solver subSolver, ProblemSplitter splitter, boolean allowMerges){this(subSolver, splitter, allowMerges, Integer.MAX_VALUE);}
    public SafePathSolver(Solver subSolver, ProblemSplitter splitter){this(subSolver, splitter, false);}
    public SafePathSolver(Solver subSolver){this(subSolver, new ColorProblemSplitter());}
    public SafePathSolver(){this(new AStar(new DistanceSumCost()));}


    public Result<Solution> solve(Problem problem) {
        return ProblemParser.parse(problem).flatMap(this::solve);
    }

    private Result<Solution> solve(StateSpace space) {
        IO.debug("Starting safe path search with splitter: %s", splitter.getClass().getSimpleName());
        var initialState = space.initialState();
        var fullProblem = space.problem();

        // Solve sub problems naively initially
        var subProblems = splitter.split(fullProblem);
        while(true){
            var result = solveSubProblems(space, initialState, fullProblem, subProblems);
            var solution = result.solution();
            if(solution.isOk() || !allowMerges || subProblems.size() == 1) return solution.map(Function.identity());

            IO.debug("Merging conflicting problems and trying again");
            subProblems = mergeMostConflictingProblems(subProblems, fullProblem, result.plans());
        }
    }

    private List<Problem> mergeMostConflictingProblems(List<Problem> problems, Problem fullProblem, SafePlan[] plans) {
        var conflictingProblems = Arrays.stream(plans)
                .sorted(new SafePlanAttemptSizeComparator())
                .limit(2)
                .map(SafePlan::getProblem)
                .toList();

        var mergedProblem = mergeProblems(conflictingProblems, fullProblem);
        return Stream.concat(problems.stream(), Stream.of(mergedProblem))
                .filter(p -> !conflictingProblems.contains(p))
                .toList();
    }

    private Problem mergeProblems(List<Problem> problems, Problem fullProblem){
        var agents = new ArrayList<Agent>();
        var boxes = new ArrayList<Box>();
        var goals = new char[fullProblem.goals.length][fullProblem.goals[0].length];
        for(var problem: problems){
            agents.addAll(problem.agents);
            boxes.addAll(problem.boxes);
            for(var agentGoal: problem.agentGoals){
                goals[agentGoal.destination.row][agentGoal.destination.col] = agentGoal.label;
            }
            for(var boxGoal: problem.boxGoals){
                goals[boxGoal.destination.row][boxGoal.destination.col] = boxGoal.label;
            }
        }

        agents.sort(Comparator.comparingInt(o -> o.label));
        return fullProblem.copyWith(agents, boxes, goals);
    }

    private SolutionPlansPair solveSubProblems(StateSpace space, State initialState, Problem fullProblem, List<Problem> subProblems) {
        var planCount = Math.max(1, subProblems.size());
        var plans = new SafePlan[planCount];
        if(subProblems.isEmpty()){
            IO.debug("No sub problems found. Initial state solution attempt with no actions.");
            var solution = new StateSolution(new State[]{initialState});
            plans[0] = new SafePlan(fullProblem, Result.ok(solution));
        } else {
            IO.debug("Found %d sub problems. Solving sub problems independently.", subProblems.size());
            var start = Stopwatch.getTimeMs();

            for(var i = 0; i < plans.length; i++){
                var subProblem = subProblems.get(i);
                var solution = subSolver.solve(subProblem).map(s -> (StateSolution)s);
                plans[i] = new SafePlan(subProblem, solution);
            }
            IO.debug("Average time per naive solution: %,.2f ms", Stopwatch.getTimeSinceMs(start)/(float)planCount);
        }

        IO.debug("Searching for conflict free solution permutations");

        var set = new HashSet<SafeAttemptPermutation>();
        var queue = new PriorityQueue<SafeAttemptPermutation>();
        queue.add(new SafeAttemptPermutation(new int[planCount], plans, space));

        while(true){
            // Verify that we can continue
            if(queue.isEmpty()){
                IO.error("All unique solution combinations have been exhausted");
                return new SolutionPlansPair(Result.error(new SolutionNotFound("No more attempt permutations")), plans);
            }

            // Check if goal is found
            var attemptPermutation = queue.poll();
            var attempts = attemptPermutation.getAttempts(plans);
//            IO.debug("Next solution permutation: %s", attemptPermutation);

            var baseAttempts = attempts.stream().map(a -> (Attempt)a).toList();
//            IO.debug(fullProblem);
            if(SolutionChecker.validAttempts(baseAttempts, space)){
                return new SolutionPlansPair(Result.ok(SolutionMerger.mergeAttempts(baseAttempts)), plans);
            }

            // Calculate all neighbor attempts
            for(var i = 0; i < planCount; i++) {
                if(attempts.get(i).getConflicts().isEmpty()) continue;

                var foreignPathIntervals = getForeignPathIntervals(attempts, i, space);
                var restrictedProblemResult = SafeProblem.from(plans[i].getProblem(), foreignPathIntervals);
                if(restrictedProblemResult.isEmpty()) continue; // no new restrictions added

                var restrictedProblem = restrictedProblemResult.get();
//                IO.debug("New restricted problem:\n"+restrictedProblem);
                var solution = subSolver.solve(restrictedProblem).map(s -> (StateSolution)s);
                if(solution.isError()){
                    continue; // unsolvable sub problem
                }

                plans[i].addAttempt(new SafeAttempt(restrictedProblem, solution));

                if(plans[i].getAttempts().size() > attemptThreshold)
                    return new SolutionPlansPair(Result.error(new SolutionNotFound("Attempt threshold exceeded")), plans);

                var next = attemptPermutation.transfer(i, plans[i].lastAttemptIndex(), plans, space);
                if (set.contains(next)) {
                    continue;
                }
                queue.add(next);
                set.add(next);
            }
        }
    }

    private List<ReservedCell> getForeignPathIntervals(List<SafeAttempt> attempts, int sourceIndex, StateSpace space) {
        var intervals = new ArrayList<ReservedCell>();
        var foreignSolutions = IntStream.range(0, attempts.size())
                .filter(i -> i != sourceIndex)
                .mapToObj(i -> attempts.get(i).getSolution())
                .filter(Result::isOk)
                .map(Result::get)
                .toList();

        for(var solution: foreignSolutions){
            var initialState = solution.getState(0);
            intervals.addAll(getStaticReserves(initialState));

            for(var step = 1; step < solution.size(); step++){
                var state = solution.getState(step);
                assert state.parent != null : "Only initial state should be orphaned";
                intervals.addAll(getStaticReserves(state));
                intervals.addAll(getActionReserves(state, state.parent, space));
            }
        }

        return intervals;
    }

    private List<ReservedCell> getStaticReserves(State state){
        return getStaticReserves(state, 2);
    }
    private List<ReservedCell> getStaticReserves(State state, int duration){
        var reserves = new ArrayList<ReservedCell>();
        for(var box: state.boxes){
            reserves.add(new ReservedCell(box.pos, new TimeInterval(state.g(), state.g()+duration)));
        }
        for(var agent: state.agents){
            reserves.add(new ReservedCell(agent.pos, new TimeInterval(state.g(), state.g()+duration)));
        }
        return reserves;
    }

    private List<ReservedCell> getActionReserves(State state, State parent, StateSpace space){
        var reserves = new ArrayList<ReservedCell>();
        for(var i = 0; i < parent.agents.size(); i++){
            var agent = parent.agents.get(i);
            var action = state.jointAction[i];
            var futureReserveResult = space.getPossibleConflictPosition(parent, agent, action);
            if(futureReserveResult.isEmpty()) continue;
            var reserve = new ReservedCell(futureReserveResult.get(), new TimeInterval(parent.g(), state.g()+1));
            reserves.add(reserve);
        }
        return reserves;
    }
}

record SolutionPlansPair(Result<StateSolution> solution, SafePlan[] plans){ }

class SafePlanAttemptSizeComparator implements Comparator<SafePlan> {
    @Override
    public int compare(SafePlan o1, SafePlan o2) {
        return Integer.compare(o1.lastAttemptIndex(), o2.lastAttemptIndex());
    }
}