package dtu.aimas.search.solutions;

import dtu.aimas.communication.IO;
import dtu.aimas.search.solvers.graphsearch.StateSpace;

public class ActionSolutionSimulator implements SolutionSimulator {
    private ActionSolution solution;
    public ActionSolutionSimulator(ActionSolution solution){
        this.solution = solution;
    }

    public boolean isValid(StateSpace stateSpace) {
        IO.info("Solution steps:");
        for(var step : this.solution.serializeSteps()){
            IO.info(step);
        }

        return true;
    }
}