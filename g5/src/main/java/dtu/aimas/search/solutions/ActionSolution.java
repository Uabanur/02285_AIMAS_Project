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
}
