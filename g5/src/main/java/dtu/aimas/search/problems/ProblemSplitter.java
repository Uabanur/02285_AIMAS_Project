package dtu.aimas.search.problems;

import dtu.aimas.search.Problem;

import java.util.List;

public interface ProblemSplitter {
    List<Problem> split(Problem problem);
}