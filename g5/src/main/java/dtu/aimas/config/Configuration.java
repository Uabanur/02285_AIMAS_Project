package dtu.aimas.config;

import dtu.aimas.search.solvers.Solver;
import dtu.aimas.search.solvers.graphsearch.BFS;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter @Setter
public class Configuration {
    public Configuration(){
        solver = new BFS();
    }

    @NonNull
    private Solver solver;

    @Override
    public String toString() {
        return String.format("Configuration{Solver: %s}", solver.getClass().getSimpleName());
    }
}
