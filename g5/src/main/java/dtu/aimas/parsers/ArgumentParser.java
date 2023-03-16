package dtu.aimas.parsers;

import java.util.ArrayList;
import java.util.Collection;

import dtu.aimas.common.Result;
import dtu.aimas.config.*;
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
        var options = new ArrayList<Result<ConfigOption>>();

        int i = 0;

        // Parse default options before any named options
        while(i < args.length && !args[i].startsWith("-")){
            tokens.add(args[i]);
            i++;
        }
        options.add(DefaultConfigOption.bind(tokens));
        tokens.clear();

        // Parse named options by option name flag '-<optionName>'
        while(i < args.length){
            var arg = args[i];

            // Collect tokens until a new config option is specified. 
            if(arg.startsWith("-")){

                if(!tokens.isEmpty()){
                    // Bind the previous config option and collect the new
                    options.add(ConfigOption.bind(tokens));
                    tokens.clear();
                }
            }

            tokens.add(arg);
            i++;
        }

        // Add potential last parsed option
        if(!tokens.isEmpty()){
            options.add(ConfigOption.bind(tokens));
            tokens.clear();
        }

        return Result.collapse(options);
    }
}
