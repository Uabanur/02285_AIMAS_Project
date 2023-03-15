package dtu.aimas;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.config.Configuration;
import dtu.aimas.parsers.ArgumentParser;
import dtu.aimas.search.solvers.graphsearch.*;

public class ArgumentParserTest {

    private Configuration assertParseOk(String... args){
        var result = ArgumentParser.parse(args);
        Assert.assertTrue(result.isOk());
        return result.get();
    }

    @Test
    public void NoArguments_Should_Pass() {
        var conf = assertParseOk();
        Assert.assertTrue("Default solver is BFS", conf.getSolver() instanceof BFS);
    }

    @Test
    public void UnknownOption_Should_Fail() {
        var result = ArgumentParser.parse(new String[]{"-unknown"});
        Assert.assertTrue("Unknown option should fail", result.isError());
    }

    @Test
    public void EmptyBFSOption_Should_Pass() {
        var conf = assertParseOk("-bfs");
        Assert.assertTrue("Expected BFS solver", conf.getSolver() instanceof BFS);
    }

    @Test 
    public void EmptyDFSOption_Should_Pass(){
        var conf = assertParseOk("-dfs");
        Assert.assertTrue("Expected DFS solver", conf.getSolver() instanceof DFS);
    }

    @Test 
    public void NonEmptyBFSOption_Should_Fail() {
        var result = ArgumentParser.parse(new String[]{"-bfs", "something"});
        Assert.assertTrue("BFS options should be empty", result.isError());
    }

    @Test 
    public void NonEmptyDFSOption_Should_Fail() {
        var result = ArgumentParser.parse(new String[]{"-dfs", "something"});
        Assert.assertTrue("DFS options should be empty", result.isError());
    }
}
