package dtu.aimas.search;

import java.util.Collection;
import java.util.Optional;

import dtu.aimas.common.Agent;
import dtu.aimas.common.Box;

public interface State {
    public Optional<Agent> getAgentAt(int row, int col);
    public Optional<Box> getBoxAt(int row, int col);
    public boolean isFree(int row, int col);
    public boolean isGoalState(Problem problem);
    public Solution getSolution();

    @Override
    int hashCode();
    @Override
    boolean equals(Object obj);
    public Collection<State> expand();
}
