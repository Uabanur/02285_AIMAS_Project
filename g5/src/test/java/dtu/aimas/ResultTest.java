package dtu.aimas;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import dtu.aimas.common.Result;
import dtu.aimas.errors.AggregateError;
import dtu.aimas.errors.SolutionNotFound;

public class ResultTest {
    private class TestClass1{}
    private class TestClass2{}

    @Test 
    public void Collapse_No_Error(){
        var results = new ArrayList<Result<Integer>>();
        results.add(Result.ok(1));
        results.add(Result.ok(2));
        results.add(Result.ok(3));

        var collapsed = Result.collapse(results);
        Assert.assertTrue("Ok results should collapse without error", collapsed.isOk());
        Assert.assertEquals("All elements should be mapped", results.size(), collapsed.get().size());
    }

    @Test 
    public void Collapse_Containing_Error(){
        var results = new ArrayList<Result<Integer>>();
        results.add(Result.ok(1));
        results.add(Result.error(new Exception()));
        results.add(Result.ok(3));
        results.add(Result.error(new Exception()));

        var collapsed = Result.collapse(results);
        Assert.assertTrue("Not all Ok results should collapse to errors", collapsed.isError());
        TestUtils.assertTypeIs(collapsed.getError().getClass(), AggregateError.class);
    }

    @Test 
    public void passError_Should_Map_Type(){
        Result<TestClass1> error1 = Result.error(new SolutionNotFound());
        Result<TestClass2> error2 = Result.passError(error1);
        TestUtils.assertTypeIs(error2.getError().getClass(), SolutionNotFound.class);
    }
}
