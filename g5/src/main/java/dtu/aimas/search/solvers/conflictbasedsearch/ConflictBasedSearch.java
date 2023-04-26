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
            IO.info("initial solution for agent " + agent.label + ": " + isolatedSolution.get().serializeSteps());
        }
        root.calculateCost();

        frontier.add(root);

        while(true){
            if(frontier.isEmpty()) 
                return Result.error(new SolutionNotFound("CBS found no solutions."));
            var node = frontier.poll();
            var conflict = node.findFirstConflict(stateSpace);
            IO.info("conflict: " + conflict.toString());
            
            if (conflict.isEmpty()){
                var sol = node.getSolution(stateSpace);
                return sol;
            }
            IO.info("involved agents: " + conflict.get().getInvolvedAgents().size());
            for(var agent: conflict.get().getInvolvedAgents()) {
                // CONSTRAINT
                IO.info("constraining for agent " + agent.label);
                var constrainedNode = node.tryConstrain(agent, conflict.get().getPosition(), conflict.get().getTimeStep());
                // If constraipreviously added, already investigated this branch
                if(constrainedNode.isEmpty()) continue;
                var constrainedProblem = ConstrainedProblem.from(initialProblem.subProblemFor(agent), constrainedNode.get().getConstraint());
                var solution = subSolver.solve(constrainedProblem);
                IO.info("Solution: ");
                IO.info(solution.get().serializeSteps());
                IO.info("Constraint: ");
                IO.info(constrainedNode.get().getConstraint().toString());
                
                constrainedNode.get().setSolutionFor(agent, solution);
                constrainedNode.get().calculateCost();
                if(constrainedNode.get().isSolvable())
                {
                    frontier.add(constrainedNode.get());
                    // TODO: works without break, otherwise infite loop
                    //break;
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
