package dtu.aimas.errors;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AggregateError extends Throwable {
    public AggregateError(Throwable... errors){
        super(generateErrorMessage(errors));
    }

    private static String generateErrorMessage(Throwable[] errors){
        return IntStream.range(0, errors.length)
         .mapToObj(i -> "Error" + String.valueOf(i+1) + ":" + errors[i].getMessage())
         .collect(Collectors.joining(" "));
    }
}
