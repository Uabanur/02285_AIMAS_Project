package dtu.aimas.config;

import java.util.Map;
import java.util.function.Supplier;

public class ConfigOptionLoader {
    public static Map<String, Supplier<ConfigOption>> getOptions(){
        return Map.of(
                BFSConfigOption.OptionName, BFSConfigOption::new,
                DFSConfigOption.OptionName, DFSConfigOption::new,
                CBSConfigOption.OptionName, CBSConfigOption::new,
                BlackboardConfigOption.OptionName, BlackboardConfigOption::new,
                SafeIntervalConfigOption.OptionName, SafeIntervalConfigOption::new,
                LoggingConfigOption.OptionName, LoggingConfigOption::new
        );
    }
}
