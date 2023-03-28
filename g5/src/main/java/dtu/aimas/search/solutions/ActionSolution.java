package dtu.aimas.search.solutions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dtu.aimas.search.Action;

public class ActionSolution implements Solution {

    private Action[][] plan;
    public ActionSolution(Action[][] plan){
        this.plan = plan;
    }

    public Collection<String> serializeSteps() {
        var steps = new ArrayList<String>();
        for(Action[] jointAction : plan){
            steps.add(Stream.of(jointAction)
                .map(a -> a.name)
                .collect(Collectors.joining("|"))
            );
        }
        return steps;
    }

    public int getMakespan() {
        return plan.length;
    }

    public int getFlowtime() {
        // TO CHANGE
        // it wrongly assumes that we have NoOp actions where eg Agent 2 is already on spot and waits for Agent 1 to finish
        if(plan.length == 0) return 0;
        int[] timeSpans = new int[plan[0].length];
        for (int agent = 0; agent < plan[0].length; agent++) {
            int step = plan[agent].length - 1;
            while(step >= 0 && plan[step][agent].type == ActionType.NoOp){
                step--;
            }
            timeSpans[agent] = step + 1;
        }
        return Arrays.stream(timeSpans).sum();
    }
}
