package dtu.aimas.parsers;

import java.util.ArrayList;
import java.util.Collection;

import dtu.aimas.common.Result;
import dtu.aimas.config.*;
import dtu.aimas.errors.UnknownArgument;
import lombok.var;

public class ArgumentParser {
    public static Result<Configuration> parse(String[] args){
        var options = parseArgs(args);
        if(options.isError()) return Result.error(options.getError());

        var conf = new Configuration();

        for (ConfigOption option : options.get())
            option.apply(conf);

        return Result.ok(conf);
    }

    private static Result<Collection<ConfigOption>> parseArgs(String[] args){
        var tokens = new ArrayList<String>();
        Result<ConfigOption> optionConfig = Result.ok(new DefaultConfigOption());
        var options = new ArrayList<Result<ConfigOption>>();

        for (String arg : args) {
            if(arg.startsWith("-")){
                options.add(optionConfig.flatMap(o -> o.bind(tokens)));
                tokens.clear();
                optionConfig = getConfigOption(arg.substring(1));
            }

            tokens.add(arg);
        }

        options.add(optionConfig.flatMap(o -> o.bind(tokens)));
        return Result.collapse(options);
    }

    private static Result<ConfigOption> getConfigOption(String optionName){
        switch (optionName) {
            case DefaultConfigOption.OptionName -> {
                return Result.ok(new DefaultConfigOption());
            }
            case BFSConfigOption.OptionName -> {
                return Result.ok(new BFSConfigOption());
            }
            case DFSConfigOption.OptionName -> {
                return Result.ok(new DFSConfigOption());
            }
            default -> {
                return Result.error(new UnknownArgument("Unknown argument given: " + optionName));
            }
        }
    }
}
