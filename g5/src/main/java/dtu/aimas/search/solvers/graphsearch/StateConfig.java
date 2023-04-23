package dtu.aimas.search.solvers.graphsearch;

import lombok.Getter;

import java.util.Objects;
import java.util.function.Function;

@Getter
public class StateConfig {
    private final Function<State, Integer> hash;
    public StateConfig(Function<State, Integer> hash){
        this.hash = hash;
    }

    public StateConfig() {
        this.hash = s -> Objects.hash(s.agents, s.boxes, s.g());
    }
}
