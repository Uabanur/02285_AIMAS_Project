package dtu.aimas.helpers;

import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;
import org.junit.Assert;

import java.io.StringReader;

public class LevelHelper {

    public static Problem getProblem(String level, String... colors) {
        return getProblem(CourseLevelParser.Instance, level, colors);
    }

    public static Problem getProblem(LevelParser parser, String level, String... colors){
        return getProblem(parser, level, "test", colors);
    }
    public static Problem getProblem(LevelParser parser, String level, String levelName, String... colors){
        assert colors.length > 0 : "Colors must be specified";
        var levelWithHeader = String.format("%s\n%s", createLevelHeader(levelName, colors), level);
        var parsed = parser.parse(new StringReader(levelWithHeader));
        Assert.assertTrue(parsed.getErrorMessageOrEmpty(), parsed.isOk());
        return parsed.get();
    }

    public static String createLevelHeader(String levelName, String... colors){
        var colorString = String.join("\n", colors);
        var template = """
                        #domain
                        hospital
                        #levelname
                        %s
                        #colors
                        %s
                        """;

        return String.format(template, levelName, colorString).trim();
    }
}
