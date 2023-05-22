package dtu.aimas.config;

import java.util.List;

import dtu.aimas.common.Result;
import dtu.aimas.communication.LogLevel;
import dtu.aimas.errors.UnknownArguments;

public class LoggingConfigOption extends ConfigOption {
    public static final String OptionName = "log";
    public String getOptionName() {
        return OptionName;
    }

    private LogLevel logLevel = LogLevel.Information;
    private boolean debugServerMessages = false;

    public void apply(Configuration conf) {
        conf.setLogLevel(logLevel);
        conf.setDebugServerMessages(debugServerMessages);
    }
    
    public Result<ConfigOption> bindInner(List<String> tokens) {
        for(var token : tokens){
            switch(token){
                case "spam" -> {logLevel = LogLevel.Spam;}
                case "debug" -> {logLevel = LogLevel.Debug;}
                case "info" -> {logLevel = LogLevel.Information;}
                case "warn" -> {logLevel = LogLevel.Warning;}
                case "error" -> {logLevel = LogLevel.Error;}
                case "servermessages" -> {debugServerMessages = true;}
                default -> {return Result.error(new UnknownArguments(token));}
            }
        }
        return Result.ok(this);
    }
}
