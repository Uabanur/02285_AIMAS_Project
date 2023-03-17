package dtu.aimas.parsers;

import java.io.Reader;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;

public abstract class LevelParser {
    protected abstract Result<Problem> parseInput(Reader level);
    public Result<Problem> parse(Reader level){
        return parseInput(level).map(p -> p.precompute());
    }
}