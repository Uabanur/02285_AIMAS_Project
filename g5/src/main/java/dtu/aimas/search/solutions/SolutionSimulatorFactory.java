package dtu.aimas.search.solutions;

import dtu.aimas.errors.UnreachableState;

public class SolutionSimulatorFactory {
    public static SolutionSimulatorFactory Instance = new SolutionSimulatorFactory();

    public SolutionSimulator getFor(Solution solution){
        if(solution instanceof ActionSolution){
            return new ActionSolutionSimulator((ActionSolution)solution);
        }

        throw new UnreachableState();
    }
}
