package dtu.aimas.search.solutions;

import dtu.aimas.search.solvers.graphsearch.State;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StateSolution implements Solution {
    private State[] states;
    public StateSolution(State[] states) {
        this.states = states;
    }

    public int size() {
        return states.length;
    }

    public State getState(int step){
        return states[step];
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
}
