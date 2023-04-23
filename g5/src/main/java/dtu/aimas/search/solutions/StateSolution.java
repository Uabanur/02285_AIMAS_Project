package dtu.aimas.search.solutions;

import dtu.aimas.search.Action;
import dtu.aimas.search.solvers.graphsearch.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StateSolution implements Solution {
    private final int productiveActions;
    private final State[] states;
    private final int hash;
    public StateSolution(State[] states) {
        this.states = states;

        var state = states[states.length-1];
        while(state.parent != null && Arrays.stream(state.jointAction).allMatch(a -> a == Action.NoOp)) {
            state = state.parent;
        }
        productiveActions = state.g();


        var jointActions = Arrays.stream(states).map(s -> s.jointAction).toArray();
        hash = Arrays.deepHashCode(jointActions);
    }

    /**
     * @return Amount of states in solutions
     */
    public int size() {
        return states.length;
    }

    /**
     * @return Amount of states ignoring trailing states with NoOp's
     */
    public int productiveSize(){
        return productiveActions + 1;
    }

    public State getState(int step){
        // clamp to [0, n]
        var index = Math.max(0, Math.min(states.length-1, step));
        return states[index];
    }

    public Collection<String> serializeSteps() {
        var steps = new ArrayList<String>(states.length);
        for(var i = 1; i < states.length; i++){
            steps.add(Stream.of(states[i].jointAction)
                .map(a -> a.name)
                .collect(Collectors.joining("|"))
            );
        }
        return steps;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return hashCode() == obj.hashCode();
    }

    public int getFlowtime() {
        // StateSolution does not support getFlowtime and getMakespan costs currently: only ActionSolution
        return 0;
    }

    public int getMakespan() {
        return 0;
    }
}
