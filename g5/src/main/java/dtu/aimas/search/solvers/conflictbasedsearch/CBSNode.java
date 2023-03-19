package dtu.aimas.search.solvers.conflictbasedsearch;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Position;
import dtu.aimas.common.Result;
import dtu.aimas.errors.NotImplemented;
import dtu.aimas.search.Solution;
import dtu.aimas.search.solvers.Constraint;
import lombok.Getter;

public class CBSNode implements Comparator<CBSNode> {
    private final Map<Agent, Result<Solution>> solutions;

    @Getter
    private final Constraint constraint;

    @Getter
    private int cost;

    public CBSNode(){
        this(new Constraint(), new HashMap<>());
    }

    public CBSNode(Constraint constraint, final Map<Agent, Result<Solution>> parentSolutions){
        solutions = Map.copyOf(parentSolutions);
        this.constraint = constraint;
    }

    public void setSolutionFor(Agent agent, Result<Solution> solution) {
        solutions.put(agent, solution);
    }

    public void calculateCost() {
        if(solutions.values().stream().anyMatch(r -> r.isError())){
            // if a subproblem is unsolvable it has max cost
            this.cost = Integer.MAX_VALUE;
            return;
        }
        
        // TODO : Add a cost function
        this.cost = 0;
    }

    @Override
    public int compare(CBSNode o1, CBSNode o2) {
        return o1.cost - o2.cost;
    }

    public Result<Solution> getSolution() {
        // TODO : Implement merging solutions
        return Result.error(new NotImplemented());
    }

    public Optional<Conflict> findConflict() {
        // TODO : Implement conflict logic
        return Optional.empty();
    }

    public CBSNode constrain(Agent agent, Position position, int timeStep) {
        return new CBSNode(constraint.extend(agent, position, timeStep), this.solutions);
    }
}
