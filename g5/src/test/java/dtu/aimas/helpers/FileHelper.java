package dtu.aimas.helpers;

import dtu.aimas.common.Result;
import dtu.aimas.communication.IO;
import dtu.aimas.parsers.CourseLevelParser;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class FileHelper {

    public static List<String> listDirectory(Path directory, String extensionFilter){
        var filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {

                return name.endsWith(extensionFilter);
            }
        };
        var files = directory.toFile().listFiles(filter);
        assert files != null : "Invalid directory given: " + directory;
        return Arrays.stream(files).map(File::getName).toList();
    }

    public static File getFile(String levelName, Path directory){
        assert directory != null;
        return getFile(levelName, directory.toFile());
    }
    public static File getFile(String levelName, File directory){
        var fileName = levelName.endsWith(".lvl") ? levelName : levelName + ".lvl";
        return new File(directory, fileName);
    }

    public static Result<Reader> getFileReader(String levelName, File directory) {
        try {
            var buffer = new FileReader(getFile(levelName, directory));
            return Result.ok(buffer);
        } catch (FileNotFoundException e) {
            return Result.error(e);
        }
    }

    public static Result<Problem> loadLevel(String levelName, Path directory) {
        return loadLevel(levelName, directory, CourseLevelParser.Instance);
    }

    public static Result<Problem> loadLevel(String levelName, Path directory, LevelParser parser) {
        return getFileReader(levelName, directory.toFile()).flatMap(parser::parse);
    }
}
