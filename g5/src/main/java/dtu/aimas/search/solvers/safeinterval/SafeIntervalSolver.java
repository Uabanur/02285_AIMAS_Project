package dtu.aimas.search.solvers.safeinterval;

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
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.DistanceSumCost;

import java.util.PriorityQueue;
import java.util.function.Function;

public class SafeIntervalSolver implements Solver {
    private final Solver subSolver;
    private final ProblemSplitter splitter;

    public SafeIntervalSolver(Solver subSolver, ProblemSplitter splitter){
        this.subSolver = subSolver;
        this.splitter = splitter;
    }
    public SafeIntervalSolver(ProblemSplitter splitter){this(new AStar(new DistanceSumCost()), splitter);}
    public SafeIntervalSolver(){this(new ColorProblemSplitter());}

    @Override
    public Result<Solution> solve(Problem initial) {
        return ProblemParser.parse(initial).flatMap(this::solve);
    }

    private Result<Solution> solve(StateSpace space) {
        var problems = splitter.split(space.getProblem());

        var start = Stopwatch.getTimeMs();
        var root = new SafeNode();

        for (var problem : problems) {
            var solution = subSolver.solve(problem).map(s -> (StateSolution)s);
            if (solution.isError()) return Result.passError(solution);
            root.bind(problem, solution);
        }

        root.calculateCost();
        IO.debug("initial solution time %d ms", Stopwatch.getTimeSinceMs(start));

        if(root.isEmpty()) return root.merge().map(Function.identity());

        var queue = new PriorityQueue<SafeNode>();
        queue.add(root);

        while(true) {
            if(queue.isEmpty())
                return Result.error(new SolutionNotFound("SafeIntervalSolver found no solutions."));

            var node = queue.poll();
            IO.debug("## New node");

            start = Stopwatch.getTimeMs();
            var conflictResult = node.firstConflict(space);
            IO.debug("first conflict time: %d ms", Stopwatch.getTimeSinceMs(start));
            if(conflictResult.isEmpty()) return node.merge().map(Function.identity());

            var conflictInterval = conflictResult.get();
            for(var problem : conflictInterval.getInvolvedProblems()){
                start = Stopwatch.getTimeMs();
                var restrictedNode = node.restrict(problem, conflictInterval);
                IO.debug("node restrict time: %d ms", Stopwatch.getTimeSinceMs(start));

                if(restrictedNode.isEmpty()) continue;

                start = Stopwatch.getTimeMs();
                var next = restrictedNode.get();
                var safeProblem = next.safeProblemFor(problem);
                var solution = subSolver.solve(safeProblem).map(s -> (StateSolution)s);
                next.bind(problem, solution);
                next.calculateCost();
                if(next.isSolvable()) queue.add(next);

                IO.debug("new solution time: %d ms", Stopwatch.getTimeSinceMs(start));
                IO.debug("new solution size: %d", solution.map(StateSolution::size).getOrElse(() -> -1));
            }
        }
    }
}
