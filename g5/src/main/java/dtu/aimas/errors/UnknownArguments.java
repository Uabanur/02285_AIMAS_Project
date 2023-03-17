package dtu.aimas.errors;

import java.util.List;
import java.util.stream.Collectors;

public class UnknownArguments extends Throwable{
    public UnknownArguments(List<String> arguments){
        super("Unknown arguments given: " + 
            arguments.stream().map(a -> "'"+a+"'").collect(Collectors.joining(", "))
        );
    }
    public UnknownArguments(String... arguments){
        this(List.of(arguments));
    }
}
