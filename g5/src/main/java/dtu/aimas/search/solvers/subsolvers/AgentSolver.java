package dtu.aimas.search.solvers.subsolvers;

import dtu.aimas.common.Result;
import dtu.aimas.parsers.ProblemParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.SolverMinLength;
import dtu.aimas.search.solvers.graphsearch.AStarMinLength;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import dtu.aimas.search.solvers.heuristics.Cost;
import dtu.aimas.search.solvers.heuristics.DefaultCost;

import java.util.function.Function;

public class AgentSolver implements SolverMinLength {
    private final Function<Cost, SolverMinLength> subSolverGenerator;
    private final Cost baseCost;

    public AgentSolver(Cost baseCost){
        this(AStarMinLength::new, baseCost);
    }
    public AgentSolver(Function<Cost, SolverMinLength> subSolverGenerator){
        this(subSolverGenerator, DefaultCost.instance);
    }

    public AgentSolver(Function<Cost, SolverMinLength> subSolverGenerator, Cost baseCost){
        this.subSolverGenerator = subSolverGenerator;
        this.baseCost = baseCost;
    }

    @Override
    public Result<Solution> solve(Problem initial) {
        return solve(initial, 0);
    }

    @Override
    public Result<Solution> solve(Problem problem, int minSolutionLength) {
        return ProblemParser.parse(problem).flatMap(space -> solve(space, minSolutionLength));
    }

    public Result<Solution> solve(StateSpace space, int minSolutionLength){
        var problem = space.getProblem();
        assert problem.agents.size() == 1 : "Agent solver only allows single agent problems";
        var agent = problem.agents.stream().findFirst().get();
        var rankedBoxes = BoxRanker.closestBox(problem, agent);
        var assignedBoxCost = new AssignedBoxGoalCost(baseCost, rankedBoxes);
        return subSolverGenerator.apply(new PendingAgentCost(assignedBoxCost, agent)).solve(problem, minSolutionLength);
    }
}
