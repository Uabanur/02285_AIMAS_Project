package dtu.aimas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static dtu.aimas.TestUtils.assertTypeIs;

import org.junit.Test;

import dtu.aimas.config.Configuration;
import dtu.aimas.errors.AggregateError;
import dtu.aimas.errors.UnknownArgument;
import dtu.aimas.parsers.ArgumentParser;
import dtu.aimas.search.solvers.graphsearch.*;

public class ArgumentParserTest {

    private Configuration assertParseOk(String... args){
        var result = ArgumentParser.parse(args);
        assertTrue(result.isOk());
        return result.get();
    }

    @Test
    public void NoArguments_Should_Pass() {
        var conf = assertParseOk();
        assertTypeIs(conf.getSolver().getClass(), BFS.class);
    }

    @Test
    public void UnknownOption_Should_Fail() {
        var result = ArgumentParser.parse(new String[]{"-unknown"});
        assertTrue("Unknown option should fail", result.isError());
    }

    @Test
    public void EmptyBFSOption_Should_Pass() {
        var conf = assertParseOk("-bfs");
        assertTypeIs(conf.getSolver().getClass(), BFS.class);
    }

    @Test 
    public void EmptyDFSOption_Should_Pass(){
        var conf = assertParseOk("-dfs");
        assertTypeIs(conf.getSolver().getClass(), DFS.class);
    }

    @Test 
    public void NonEmptyBFSOption_Should_Fail() {
        var result = ArgumentParser.parse(new String[]{"-bfs", "something"});
        assertTrue("BFS options should be empty", result.isError());

        var resultError = result.getError();
        assertTypeIs(resultError.getClass(), AggregateError.class);

        var errors = ((AggregateError)resultError).getErrors();
        assertEquals("Expected exactly 1 error", errors.length, 1);
        assertTypeIs(errors[0].getClass(), UnknownArgument.class);
    }

    @Test 
    public void NonEmptyDFSOption_Should_Fail() {
        var result = ArgumentParser.parse(new String[]{"-dfs", "something"});
        assertTrue("DFS options should be empty", result.isError());
    }

    @Test
    public void TwoValidOptions_Should_Pass() {
        var conf = assertParseOk("-bfs", "-dfs");
        assertTrue("Expected DFS solver", conf.getSolver() instanceof DFS);
    }
}
