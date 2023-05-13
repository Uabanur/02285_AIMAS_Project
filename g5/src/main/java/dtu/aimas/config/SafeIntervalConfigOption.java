package dtu.aimas.config;

import dtu.aimas.common.Result;
import dtu.aimas.errors.UnknownArguments;
import dtu.aimas.search.solvers.safeinterval.SafeIntervalSolver;

import java.util.List;

public class SafeIntervalConfigOption extends ConfigOption{
    public static final String OptionName = "si";
    public String getOptionName() {return OptionName;}

    public void apply(Configuration conf) {
        conf.setSolver(new SafeIntervalSolver());
    }

    public Result<ConfigOption> bindInner(List<String> tokens) {
        if(!tokens.isEmpty())
            return Result.error(
                    new UnknownArguments(tokens));

        return Result.ok(this);
    }
}
