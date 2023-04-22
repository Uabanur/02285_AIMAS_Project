package dtu.aimas.search.solutions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dtu.aimas.search.Action;
import dtu.aimas.search.ActionType;

public class ActionSolution implements Solution {

    private Action[][] plan;
    public ActionSolution(Action[][] plan){
        this.plan = plan;
    }

    public int size() {
        return plan.length;
    }

    public Action[] getJointAction(int step){
        return plan[step];
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
        if(plan.length == 0) return 0;
        int[] costs = new int[plan[0].length];
        for (int agent = 0; agent < plan[0].length; agent++) {
            int step = plan[agent].length;
            while(step >= 0 && plan[step][agent].type == ActionType.NoOp){
                step--;
            }
            costs[agent] = step + 1;
        }
        return Arrays.stream(costs).sum();
    }
}
