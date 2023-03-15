package dtu.aimas.config;

import java.util.List;

import dtu.aimas.common.Result;
import dtu.aimas.errors.UnknownArgument;
import dtu.aimas.search.solvers.graphsearch.BFS;

public class BFSConfigOption extends ConfigOption {
    public static final String OptionName = "bfs";
    public String getOptionName() {
        return OptionName;
    }

    public void apply(Configuration conf) {
        conf.setSolver(new BFS());
    }
    
    public Result<ConfigOption> bindInner(List<String> tokens) {
        if(!tokens.isEmpty()) 
            return Result.error(
                new UnknownArgument("Unknown arguments for BFS: " + String.join(", ", tokens)));

        return Result.ok(this);
    }
}
