package dtu.aimas.helpers;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class FileHelper {
    public static Result<Reader> getFileReader(String levelName){
        try {
            var levelFile = new File(IO.LevelDir.toFile(), levelName + ".lvl");
            var buffer = new FileReader(levelFile);
            return Result.ok(buffer);
        } catch (FileNotFoundException e) {
            return Result.error(e);
        }
    }


    public static Result<Problem> loadLevel(String levelName, LevelParser parser) {
        return getFileReader(levelName).flatMap(parser::parse);
    }
}
