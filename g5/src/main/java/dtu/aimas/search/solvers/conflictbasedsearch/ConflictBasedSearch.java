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
import dtu.aimas.search.problems.AgentBoxAssignationSplitter;
import dtu.aimas.search.problems.ProblemSplitter;

public class ConflictBasedSearch implements Solver {
    private Solver subSolver;
    private AgentBoxAssignationSplitter splitter;

    public ConflictBasedSearch(Solver subSolver){
        this.subSolver = subSolver;
        this.splitter = new AgentBoxAssignationSplitter();
    }

    public Result<Solution> solve(Problem initialProblem) {
        ArrayList<Agent> agents = new ArrayList<Agent>(initialProblem.agents);
        ArrayList<Box> boxes = new ArrayList<Box>(initialProblem.boxes);
        State initialState = new State(agents, boxes);
        StateSpace stateSpace = new StateSpace(initialProblem, initialState);

        var frontier = new PriorityQueue<CBSNode>();
        var root = new CBSNode();

        var subProblems = splitter.split(initialProblem);
        for(Problem subProblem : subProblems){
            var isolatedSolution = subSolver.solve(subProblem);
            if (isolatedSolution.isError()) 
                return Result.passError(isolatedSolution);
            Agent agent = subProblem.agents.iterator().next();
            root.setSolutionFor(agent, isolatedSolution);
        }
        root.calculateCost();

        frontier.add(root);

        while(true){
            if(frontier.isEmpty()) 
                return Result.error(new SolutionNotFound("CBS found no solutions."));
            var node = frontier.poll();

            var conflict = node.findFirstConflict(stateSpace);
            if (conflict.isEmpty()) return node.getSolution(stateSpace);

            for(var agent: conflict.get().getInvolvedAgents()) {
                var constrainedNode = node.tryConstrain(agent, conflict.get().getPosition(), conflict.get().getTimeStep());
                
                // CASE: constraint previously added, already investigated this branch
                if(constrainedNode.isEmpty()) continue;
                
                var constrainedProblem = ConstrainedProblem.from(splitter.subProblemForAgent(agent), constrainedNode.get().getConstraint());
                var solution = subSolver.solve(constrainedProblem);
                constrainedNode.get().setSolutionFor(agent, solution);
                constrainedNode.get().calculateCost();

                if(constrainedNode.get().isSolvable()) frontier.add(constrainedNode.get());
            }
        }
    }
}
