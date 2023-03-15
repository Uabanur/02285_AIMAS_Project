package dtu.aimas.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dtu.aimas.Constants;
import dtu.aimas.common.Result;
import dtu.aimas.parsers.LevelParser;
import dtu.aimas.search.Problem;
import dtu.aimas.search.Solution;

public class IO {

    public final static String UserDir = System.getProperty("user.dir");
    public final static String LogDir = String.join(File.separator, UserDir, "logs");
    // public final static String SearchClientDir = String.join(File.separator, UserDir, "searchclient");
    // public final static String LevelDir = String.join(File.separator, SearchClientDir, "levels");

    public static LogLevel logLevel = LogLevel.Information;
    static boolean debugServerMessages = false;

    private static boolean logOutputToFile = false;
    private static boolean serverCommunicationInitialized = false;
    private static BufferedWriter writer = null;
    private static BufferedReader serverMessages = new BufferedReader(
        new InputStreamReader(System.in, StandardCharsets.US_ASCII));


    private static void sendToServerRaw(String msg){
        if (serverCommunicationInitialized){
            System.out.println(msg);
        }
    }

    private static void log(String msg){
        var logMessage = "[client]" + msg;

        if (serverCommunicationInitialized){
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

    public static void debug(Object o) {debug(o.toString());}
    public static void debug(String msg){
        if (logLevel == LogLevel.Debug){
            log("[debug] "+msg);
        }
    }
    public static void debug(String format, Object... args){
        debug(String.format(format, args));
    }

    public static void info(Object o){info(o.toString());}
    public static void info(String msg){
        if (logLevel == LogLevel.Information || logLevel == LogLevel.Debug) {
            log("[info] "+msg);
        }
    }
    public static void info(String format, Object... args){
        info(String.format(format, args));
    }

    public static void warn(Object o){warn(o.toString());}
    public static void warn(String msg){
        if (logLevel == LogLevel.Error) return;
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
    public static void logException(Throwable e) {
        var traceElements = Stream.of(e.getStackTrace()).map(t -> t.toString()).collect(Collectors.toList());
        var traceString = String.join("\n\tat ", traceElements);
        error("%s\n%s", e.getMessage(), traceString);
    }

    public static void logOutputToFile(String logSpecifier) {
        closeLogOutput();

        var currentDate = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        var logName = String.format("%s_%s.log", logSpecifier, currentDate);
        var logPath = String.join(File.separator, LogDir, logName);
        var logFile = new File(logPath);

        try {
            if (!logFile.exists()){
                info("Creating log file: \n%s", logFile.getAbsolutePath());
                Files.createDirectories(Paths.get(LogDir));
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

    public static Result<Problem> initializeServerCommunication(LevelParser levelParser) 
    throws IOException{
        serverCommunicationInitialized = true;
        sendToServerRaw(Constants.GroupName);
        info("Client name: " + Constants.GroupName);
        return levelParser.parse(serverMessages);
    }

    public static Result<Solution> sendSolutionToServer(Solution solution) {
        try {

            for (var step : solution.steps()) {
                var strategy = step.serialize();
                sendToServerRaw(strategy);

                if (debugServerMessages){
                    debug("Strategy: " + strategy);
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