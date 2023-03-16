package dtu.aimas.config;

import java.util.List;

import dtu.aimas.common.Result;
import dtu.aimas.errors.UnknownArgument;

public class DefaultConfigOption extends ConfigOption {
    public static final String OptionName = "default";
    public String getOptionName() {
        return OptionName;
    }

    public void apply(Configuration conf) { }

    public static Result<ConfigOption> bind(List<String> tokens){
        return new DefaultConfigOption().bindInner(tokens);
    }

    protected Result<ConfigOption> bindInner(List<String> tokens) {
        if(!tokens.isEmpty()) 
            return Result.error(
                new UnknownArgument("Unknown arguments: " + String.join(", ", tokens)));

        return Result.ok(this);
    }
}
