package dtu.aimas.search.solvers.conflictbasedsearch;

import java.util.ArrayList;
import java.util.PriorityQueue;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;
import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.errors.SolutionNotFound;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.State;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class ConflictBasedSearch implements Solver {
    private Solver subSolver;

    public ConflictBasedSearch(Solver subSolver){
        this.subSolver = subSolver;
    }

    public Result<Solution> solve(Problem initialProblem) {
        ArrayList<Agent> agents = new ArrayList<Agent>(initialProblem.agents);
        ArrayList<Box> boxes = new ArrayList<Box>(initialProblem.boxes);
        State initialState = new State(agents, boxes);
        StateSpace stateSpace = new StateSpace(initialProblem, initialState);

        var frontier = new PriorityQueue<CBSNode>();
        var root = new CBSNode();

        for(var agent : initialProblem.agents){
            var isolatedSolution = subSolver.solve(initialProblem.subProblemFor(agent));
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
            var conflict = node.findFirstConflict(stateSpace);

            if (conflict.isEmpty()){
                var sol = node.getSolution(stateSpace);
                return sol;
            }
                
            for(var agent: conflict.get().getInvolvedAgents()) {
                // IO.info("agent" + agent.label + " is involved");
                // CONSTRAINT
                var constrainedNode = node.constrain(agent, conflict.get().getPosition(), conflict.get().getTimeStep());
                var constrainedProblem = ConstrainedProblem.from(initialProblem.subProblemFor(agent), constrainedNode.getConstraint());
                var solution = subSolver.solve(constrainedProblem);
                constrainedNode.setSolutionFor(agent, solution);
                constrainedNode.calculateCost();
                if(constrainedNode.isSolvable())
                {
                    // IO.info("Constrained solvable!");
                    frontier.add(constrainedNode);
                    break;
                }
                // TODO: test with correct conflict detector whether needed or not, if not, remove
                // // NEGATE CONSTRAINT
                // var unconstrainedNode = node;
                // var unconstrainedProblem = initialProblem.subProblemFor(agent);
                // solution = subSolver.solve(unconstrainedProblem);
                // unconstrainedNode.setSolutionFor(agent, solution);
                // unconstrainedNode.calculateCost();
                // if(unconstrainedNode.isSolvable())
                // {
                //     frontier.add(unconstrainedNode);
                //     IO.info("Unconstrained solvable!");
                //     break;
                // }
                // IO.info("No subproblem is solvable!");
                // // BACKTRACK to the conflict and try different resolution
            }
        }
    }
}
