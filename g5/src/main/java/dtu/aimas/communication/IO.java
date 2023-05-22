package dtu.aimas.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dtu.aimas.SearchClient;
import dtu.aimas.common.Result;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.solutions.Solution;

public class IO {
    private final static String CurrentClassPathString = getClassPath();
    private final static Path TargetsPath = getTargetsPath();

    private static Path getTargetsPath() {
        if(CurrentClassPathString == null) return null;
        return Paths.get(CurrentClassPathString).getParent();
    }

    public final static Path TargetClassesPath = getTargetClassesPath();

    private static Path getTargetClassesPath() {
        if (TargetsPath == null) return null;
        return Paths.get(TargetsPath.toString(), "classes");
    }

    public final static Path LogDir = getLogDir();

    private static Path getLogDir() {
        if(TargetsPath == null) return null;
        return Paths.get(TargetsPath.toString(), "logs");
    }

    public final static Path LevelDir = getLevelDir();

    private static Path getLevelDir() {
        if (TargetClassesPath == null) return null;
        return Paths.get(TargetClassesPath.toString(), "levels");
    }

    public final static Path CompLevelDir = getCompLevelDir();

    private static Path getCompLevelDir() {
        if(LevelDir == null) return null;
        return Paths.get(LevelDir.toString(), "complevels");
    }

    public final static Path G5Dir = getG5Dir();
    private static Path getG5Dir() {
        if(TargetsPath == null) return null;
        return TargetsPath.getParent();
    }


    public static LogLevel logLevel = LogLevel.Information;
    public static boolean debugServerMessages = false;

    private static boolean logOutputToFile = false;
    private static boolean useServerCommunication = false;
    private static BufferedWriter writer = null;
    private static BufferedReader serverMessages = new BufferedReader(
        new InputStreamReader(System.in, StandardCharsets.US_ASCII));


    private static String getClassPath(){
        var classPaths = System.getProperty("java.class.path").split(OsClassPathSeparator.get());
        var path = Arrays.stream(classPaths).filter(p -> p.contains("g5")).findAny();
        return path.orElse(null);
    }

    private static void sendToServerRaw(String msg){
        if (useServerCommunication){
            System.out.println(msg);
        }
    }

    private static void log(String msg){
        var logMessage = "[client]" + msg;

        if (useServerCommunication){
            System.err.println(logMessage);
        } else {
            System.out.println(logMessage);
        }

        if (logOutputToFile){
            assert writer != null;

            try {
                writer.write(logMessage);
                if(!logMessage.endsWith("\n")){
                    writer.write("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void spam(Object o) {spam(o.toString());}
    public static void spam(String msg){
        if (logLevel.ordinal() > LogLevel.Spam.ordinal()) return;
        log("[spam] "+msg);
    }
    public static void spam(String format, Object... args){
        spam(String.format(format, args));
    }

    public static void debug(Object o) {debug(o.toString());}
    public static void debug(String msg){
        if (logLevel.ordinal() > LogLevel.Debug.ordinal()) return;
        log("[debug] "+msg);
    }
    public static void debug(String format, Object... args){
        debug(String.format(format, args));
    }

    public static void info(Object o){info(o.toString());}
    public static void info(String msg){
        if (logLevel.ordinal() > LogLevel.Information.ordinal()) return;
        log("[info] "+msg);
    }
    public static void info(String format, Object... args){
        info(String.format(format, args));
    }

    public static void warn(Object o){warn(o.toString());}
    public static void warn(String msg){
        if (logLevel.ordinal() > LogLevel.Warning.ordinal()) return;
        log("[warn] "+msg);
    }
    public static void warn(String format, Object... args){
        warn(String.format(format, args));
    }

    public static void error(Object o){error(o.toString());}
    public static void error(String msg){
        log("[error] " +msg);
    }
    public static void error(String format, Object... args){
        error(String.format(format, args));
    }

    public static void logException(Throwable e) { logException(e, true, true); }
    public static void logException(Throwable e, boolean printSuppressed, boolean printCause) {
        var traceElements = Stream.of(e.getStackTrace()).map(t -> t.toString()).collect(Collectors.toList());
        var traceString = String.join("\n\tat ", traceElements);
        error("%s\n%s", e.getMessage(), traceString);

        if(printSuppressed){
            for (Throwable suppressed : e.getSuppressed()) {
                IO.error("Child error:");

                // Child errors are not recursed
                IO.logException(suppressed, false, false);
            }
        }

        if(printCause){
            Throwable cause = e.getCause();
            if(cause != null){
                IO.error("With cause:");
                IO.logException(cause, false, false);
            }
        }
    }

    public static void logOutputToFile(String logSpecifier) {
        closeLogOutput();

        var currentDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        var logName = String.format("%s_%s.log", logSpecifier, currentDate);
        var logFile = new File(LogDir.toFile(), logName);

        try {
            if (!logFile.exists()){
                info("Creating log file: \n%s", logFile.getAbsolutePath());
                Files.createDirectories(LogDir);
                logFile.createNewFile();
            }

            logOutputToFile = true;
            writer = new BufferedWriter(new FileWriter(logFile));

        } catch (IOException e){
            IO.closeLogOutput();
            IO.error("Failed to log output to file");
            IO.logException(e);
        }
    }

    public static void closeLogOutput() {
        logOutputToFile = false;
        if (writer == null) return;
        try {
            writer.close();
        } catch (IOException e) {
            IO.logException(e);
        }
        writer = null;
    }

    public static void useServerCommunication(){
        useServerCommunication = true;
    }

    public static Result<Problem> initializeServerCommunication(LevelParser levelParser) {
        assert useServerCommunication;

        final String groupName = SearchClient.config.getGroupName();
        sendToServerRaw(groupName);
        info("Client name: " + groupName);
        return levelParser.parse(serverMessages);
    }

    public static Result<Solution> sendSolutionToServer(Solution solution) {
        try {

            for (var step : solution.serializeSteps()) {
                sendToServerRaw(step);

                if (debugServerMessages){
                    debug("Strategy: " + step);
                }

                var response = serverMessages.readLine();

                if (debugServerMessages){
                    debug("Strategy response: " + response);
                }
            }

            return Result.ok(solution);

        } catch (IOException e) {
            return Result.error(e);
        }
    }
}