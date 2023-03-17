package dtu.aimas;

import java.text.MessageFormat;

import org.junit.Assert;

public class TestUtils {
    public static <T, U> void assertTypeIs(Class<T> actual, Class<U> expected){
        Assert.assertTrue(
            MessageFormat.format("Expected type {0} got type {1}", 
                expected.getSimpleName(), actual.getSimpleName()), 
            expected.isAssignableFrom(actual));
    }
}
