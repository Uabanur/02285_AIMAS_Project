package dtu.aimas.parsers;

import java.io.Reader;

import dtu.aimas.common.Result;
import dtu.aimas.search.Problem;

public interface LevelParser {
    public Result<Problem> parse(Reader level);
}