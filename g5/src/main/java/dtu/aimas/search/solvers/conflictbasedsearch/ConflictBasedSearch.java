package dtu.aimas.search.solvers.conflictbasedsearch;

import java.util.PriorityQueue;

import dtu.aimas.common.Result;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;
import dtu.aimas.search.solvers.Solver;

public class ConflictBasedSearch implements Solver {
    private Solver subSolver;

    public ConflictBasedSearch(Solver subSolver){
        this.subSolver = subSolver;
    }

    public Result<Solution> solve(Problem initial) {
        var frontier = new PriorityQueue<CBSNode>();
        var root = new CBSNode();

        for(var agent : initial.agents){
            var isolatedSolution = subSolver.solve(initial.subProblemFor(agent));
            if (isolatedSolution.isError()) 
                return Result.passError(isolatedSolution);

            root.setSolutionFor(agent, isolatedSolution);
        }
        root.calculateCost();

        frontier.add(root);

        while(true){
            if(frontier.isEmpty()) 
                return Result.error(new SolutionNotFound("CBS found no solutions."));

            var node = frontier.poll();
            var issues = node.findConflict();
            if (issues.isEmpty())
                return node.getSolution();

            var conflict = issues.get();
            for(var agent: conflict.getInvolvedAgents()) {
                var childNode = node.constrain(agent, conflict.getPosition(), conflict.getTimeStep());
                
                var constrainedProblem = ConstrainedProblem.from(
                    initial.subProblemFor(agent), childNode.getConstraint());

                var solution = subSolver.solve(constrainedProblem);
                childNode.setSolutionFor(agent, solution);
                childNode.calculateCost();

                if(childNode.isSolvable())
                    frontier.add(childNode);
            }
        }
    }
}
