package dtu.aimas.search.solutions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dtu.aimas.communication.IO;
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
        int cost = 0;
        if(plan.length == 0){
            cost = 0;
        }
        else{
            for(int i = 0; i<plan.length; i++){
                for(int j = 0; j<plan[0].length; j++){
                    cost++;
                }
            }
        }
        return cost;
    }
}
