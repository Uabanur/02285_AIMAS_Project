package dtu.aimas.config;

import java.util.List;

import dtu.aimas.common.Result;
import dtu.aimas.errors.InvalidArgument;

public abstract class ConfigOption {
    public abstract String getOptionName();
    public abstract void apply(Configuration conf);
    protected abstract Result<ConfigOption> bindInner(List<String> tokens);

    public Result<ConfigOption> bind(List<String> tokens){
        if (tokens.isEmpty()) 
            return Result.error(
                new InvalidArgument("Config option tokens must include option name"));

        var optionNameFlag = tokens.get(0);
        if (!optionNameFlag.startsWith("-")) 
            return Result.error(
                new InvalidArgument("Config option name flag must start with '-'"));

        if (!optionNameFlag.substring(1).equals(getOptionName()))
            return Result.error(
                new InvalidArgument("Wrong option name flag given: " + optionNameFlag+". Expected: " + getOptionName()));
    
        return bindInner(tokens.subList(1, tokens.size()));
    }
}
