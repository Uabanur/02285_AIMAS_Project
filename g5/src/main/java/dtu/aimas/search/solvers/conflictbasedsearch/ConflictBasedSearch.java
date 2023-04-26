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
            
            IO.info("Before constraint:");
            IO.info(node.toString());

            var conflict = node.findFirstConflict(stateSpace);
            
            if (conflict.isEmpty()) return node.getSolution(stateSpace);

            IO.info(conflict.get().toString());

            for(var agent: conflict.get().getInvolvedAgents()) {
                IO.info("Constraining for agent " + agent.label);
                var constrainedNode = node.tryConstrain(agent, conflict.get().getPosition(), conflict.get().getTimeStep());
                
                // CASE: constraint previously added, already investigated this branch
                if(constrainedNode.isEmpty()) continue;

                var constrainedProblem = ConstrainedProblem.from(initialProblem.subProblemFor(agent), constrainedNode.get().getConstraint());
                var solution = subSolver.solve(constrainedProblem);
                constrainedNode.get().setSolutionFor(agent, solution);
                constrainedNode.get().calculateCost();

                IO.info("After constraint:");
                IO.info(constrainedNode.get().toString());

                if(constrainedNode.get().isSolvable()) frontier.add(constrainedNode.get());
            }
        }
    }
}
