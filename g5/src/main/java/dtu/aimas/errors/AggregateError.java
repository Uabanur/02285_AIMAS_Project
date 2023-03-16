package dtu.aimas.errors;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.Getter;

public class AggregateError extends Throwable {
    @Getter
    private Throwable[] errors;

    public AggregateError(Throwable... errors){
        super(generateErrorMessage(errors));
        this.errors = errors;

        for (Throwable error : errors) {
            addSuppressed(error);
        }
    }

    private static String generateErrorMessage(Throwable[] errors){
        return IntStream.range(0, errors.length)
         .mapToObj(i -> "Error " + String.valueOf(i+1) + ": " + errors[i].getMessage())
         .collect(Collectors.joining(" "));
    }
}
