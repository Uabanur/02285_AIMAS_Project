package dtu.aimas.search.solvers.conflictbasedsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Position;
import dtu.aimas.common.Result;
import dtu.aimas.errors.NotImplemented;
import dtu.aimas.search.Action;
import dtu.aimas.search.solutions.ActionSolution;
import dtu.aimas.search.solutions.Solution;
import dtu.aimas.search.solvers.Constraint;
import dtu.aimas.search.solvers.graphsearch.StateSpace;
import lombok.Getter;

public class CBSNode implements Comparable<CBSNode> {
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
        // makespan
        this.cost = solutions.values().stream().mapToInt(r -> r.get().getMakespan()).max().orElse(MAX_COST);

        // flowtime
        //this.cost = solutions.values().stream().mapToInt(r -> r.get().getFlowtime()).max().orElse(MAX_COST);
    }

    public boolean isSolvable() {
        return cost < MAX_COST;
    }

    public Result<Solution> getSolution(StateSpace stateSpace) {
        var subSolutionsResult = Result.collapse(solutions.values());
        assert subSolutionsResult.isOk() : subSolutionsResult.getError().getMessage();

        // Delegate retrieval of the merge solution to the state space
        List<Map.Entry<Agent, Result<Solution>>> sortedSolutions = getSortedSolutions();
        Action[][] mergedPlan = stateSpace.extractPlanFromSubsolutions(sortedSolutions);

        return Result.ok(new ActionSolution(mergedPlan));
    }

    public ArrayList<Conflict> findConflicts(StateSpace stateSpace) {
        var subSolutionsResult = Result.collapse(solutions.values());
        assert subSolutionsResult.isOk() : subSolutionsResult.getError().getMessage();

        // Delegate the conflict detection to the state space
        List<Map.Entry<Agent, Result<Solution>>> sortedSolutions = getSortedSolutions();
        return stateSpace.replaySolutionsForConflicts(sortedSolutions);
    }

    private List<Map.Entry<Agent, Result<Solution>>> getSortedSolutions() {

        // Sort the solution map by agent's labels, so that the individual agent actions can be applied in correct order
        List<Map.Entry<Agent, Result<Solution>>> entryList = new ArrayList<>(((Map) solutions).entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<Agent, Result<Solution>>>() {
            @Override
            public int compare(Map.Entry<Agent, Result<Solution>> entry1, Map.Entry<Agent, Result<Solution>> entry2) {
                return entry1.getKey().label - entry2.getKey().label;
            }
        });

        return entryList;
    }

    public CBSNode constrain(Agent agent, Position position, int timeStep) {
        var solutionsCopy = Map.copyOf(this.solutions);
        var extendedConstraints = constraint.extend(agent, position, timeStep);
        return new CBSNode(extendedConstraints, solutionsCopy);
    }

    @Override
    public int compareTo(CBSNode other) {
        return this.cost - other.cost;
    }
}
