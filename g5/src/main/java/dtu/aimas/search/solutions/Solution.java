package dtu.aimas.search.solutions;

import java.util.Collection;

public interface Solution {
    public Collection<String> serializeSteps();
    public int getFlowtime();
    public int getMakespan();
    public int size();
}
