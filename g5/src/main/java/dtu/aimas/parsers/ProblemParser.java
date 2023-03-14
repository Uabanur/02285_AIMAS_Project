package dtu.aimas.parsers;

import dtu.aimas.common.Result;
import dtu.aimas.errors.NotImplemented;
import dtu.aimas.search.Problem;
import dtu.aimas.search.State;

public class ProblemParser {
    public static Result<State> parse(Problem problem){
        return Result.error(new NotImplemented());
    }
}
