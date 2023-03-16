package dtu.aimas.config;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import dtu.aimas.common.Result;
import dtu.aimas.errors.InvalidArgument;
import dtu.aimas.errors.UnknownArgument;

public abstract class ConfigOption {
    private static final Map<String, Supplier<ConfigOption>> options = Map.of(
        BFSConfigOption.OptionName, BFSConfigOption::new,
        DFSConfigOption.OptionName, DFSConfigOption::new
    );

    public abstract String getOptionName();
    public abstract void apply(Configuration conf);
    protected abstract Result<ConfigOption> bindInner(List<String> tokens);

    public static Result<ConfigOption> bind(List<String> tokens){
        if (tokens.isEmpty()) 
            return Result.error(
                new InvalidArgument("Config option tokens must include option name"));

        var optionNameFlag = tokens.get(0);
        if (!optionNameFlag.startsWith("-")) 
            return Result.error(
                new InvalidArgument("Config option name flag must start with '-'"));

        return bindConfigOption(optionNameFlag.substring(1), tokens.subList(1, tokens.size()));
    }

    private static Result<ConfigOption> bindConfigOption(String optionName, List<String> tokens){
        var option = Result.ofNullable(
                options.get(optionName), 
                () -> new UnknownArgument("Unknown argument given: " + optionName)
            ).map(o -> o.get());

        return option.flatMap(o -> o.bindInner(tokens));
    }
}
