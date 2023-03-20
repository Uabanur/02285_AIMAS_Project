package dtu.aimas.benchmarks;

import org.openjdk.jmh.annotations.Benchmark;
import dtu.aimas.parsers.ArgumentParser;

public class ArgumentParserBenchmark {
    @Benchmark
    public void NoArguments(){
            ArgumentParser.parse(new String[]{});
    } 

    @Benchmark
    public void BFSArgument(){
            ArgumentParser.parse(new String[]{"-bfs"});
    } 
}
