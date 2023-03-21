package dtu.aimas.search.solvers.conflictbasedsearch;

import java.util.Collection;
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
    private static final int MAX_COST = Integer.MAX_VALUE;
    private final Map<Agent, Result<Solution>> solutions;

    @Getter
    private final Constraint constraint;

    private int cost;

    public CBSNode(){
        this(Constraint.empty(), new HashMap<>());
    }

    public CBSNode(Constraint constraint, final Map<Agent, Result<Solution>> solutions){
        this.solutions = solutions;
        this.constraint = constraint;
        this.cost = Integer.MAX_VALUE;
    }

    public void setSolutionFor(Agent agent, Result<Solution> solution) {
        solutions.put(agent, solution);
    }

    public void calculateCost() {
        if(solutions.values().stream().anyMatch(r -> r.isError())){
            // if a subproblem is unsolvable it has max cost
            this.cost = MAX_COST;
            return;
        }
        
        // TODO : Add a cost function
        this.cost = 0;
    }

    @Override
    public int compare(CBSNode o1, CBSNode o2) {
        return o1.cost - o2.cost;
    }

    public boolean isSolvable() {
        return cost < MAX_COST;
    }

    public Result<Solution> getSolution() {
        // TODO : Implement merging solutions
        return Result.error(new NotImplemented());
    }

    public Optional<Conflict> findConflict() {
        var subSolutionsResult = Result.collapse(solutions.values());
        assert subSolutionsResult.isOk() : subSolutionsResult.getError().getMessage();
        return replaySolutionsForConflicts(subSolutionsResult.get());
    }

    private Optional<Conflict> replaySolutionsForConflicts(Collection<Solution> solutions){
        // TODO : Implement conflict logic
        // simulate the plans from the different solutions
        return Optional.empty();
    }

    public CBSNode constrain(Agent agent, Position position, int timeStep) {
        var solutionsCopy = Map.copyOf(this.solutions);
        var extendedConstraints = constraint.extend(agent, position, timeStep);
        return new CBSNode(extendedConstraints, solutionsCopy);
    }
}
