package dtu.aimas.search.solutions;

import dtu.aimas.communication.IO;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class StateSolutionSimulator implements SolutionSimulator {
    private StateSolution solution;
    public StateSolutionSimulator(StateSolution solution){
        this.solution = solution;
    }

    @Override
    public boolean isValid(StateSpace stateSpace) {
    
        IO.info("Solution steps:");
        for(var step : this.solution.serializeSteps()){
            IO.info(step);
        }

        return true;
    }
}
