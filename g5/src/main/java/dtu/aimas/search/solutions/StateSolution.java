package dtu.aimas.search.solutions;

import dtu.aimas.search.solvers.graphsearch.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StateSolution implements Solution {
    private State[] states;
    private final int hash;
    public StateSolution(State[] states) {
        this.states = states;

        var jointActions = Arrays.stream(states).map(s -> s.jointAction).toArray();
        hash = Arrays.deepHashCode(jointActions);
    }

    public int size() {
        return states.length;
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
}
