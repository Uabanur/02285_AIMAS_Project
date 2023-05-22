package dtu.aimas.config;

import dtu.aimas.common.Result;
import dtu.aimas.errors.UnknownArguments;
import dtu.aimas.search.solvers.graphsearch.AStar;
import dtu.aimas.search.solvers.heuristics.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class AStarConfigOption extends ConfigOption{
    public static final String OptionName = "astar";
    public String getOptionName() {return OptionName;}

    private Cost cost = new GoalCount();

    @Override
    public void apply(Configuration conf) {
        conf.setSolver(new AStar(cost));
    }

    @Override
    protected Result<ConfigOption> bindInner(List<String> tokens) {
        for(var token: tokens){
            if(token.startsWith("cost:")) {
                String fullCostName = null;
                try {
                    var costName = token.substring("cost:".length());
                    fullCostName = costName.startsWith("dtu")
                            ? costName
                            : GoalCount.class.getPackageName() + "." + costName;
                    cost = (Cost) Class.forName(fullCostName).getDeclaredConstructor().newInstance();
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    return Result.error(new IllegalArgumentException("Cost function not found: " + fullCostName));
                }
                continue;
            }

            return Result.error(new UnknownArguments(tokens));
        }

        return Result.ok(this);
    }
}
