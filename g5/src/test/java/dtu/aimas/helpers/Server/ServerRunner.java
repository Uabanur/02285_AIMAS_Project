package dtu.aimas.helpers.Server;

import dtu.aimas.SearchClient;
import dtu.aimas.communication.IO;
import dtu.aimas.helpers.FileHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ServerRunner
        implements LevelConfiguredServerRunner, ClientConfiguredServerRunner, ServerConfiguredServerRunner {
    private final String levelName;
    private final Path directory;
    private String clientConfigs = "";
    private ServerConfig serverConfigs;

    private ServerRunner(String levelName, Path directory){
        this.levelName = levelName;
        this.directory = directory;
    }

    public static LevelConfiguredServerRunner level(String levelName, Path directory){
        assert levelName != null;
        assert directory != null;
        return new ServerRunner(levelName, directory);
    }

    @Override
    public ClientConfiguredServerRunner clientConfigs(String configs){
        this.clientConfigs = configs;
        return this;
    }

    @Override
    public ServerConfiguredServerRunner configureServer(Consumer<ServerConfig> config) {
        this.serverConfigs = new ServerConfig();
        config.accept(this.serverConfigs);
        return this;
    }

    @Override
    public void Run() {
        assert IO.G5Dir != null;
        assert IO.TargetClassesPath != null;

        try {
            var mainClass = SearchClient.class.getName();
            var file = FileHelper.getFile(levelName, directory);
            var execDir = IO.G5Dir.toFile();
            var classPath = "." + IO.TargetClassesPath.toFile().getAbsolutePath()
                    .substring(execDir.getAbsolutePath().length());

            IO.info("Running server with following settings:");
            IO.info("Class path: %s", classPath);
            IO.info("Main class: %s", mainClass);
            IO.info("Level: %s", levelName);
            IO.info("Directory: %s", directory.getFileName());
            IO.info("Client configs: %s", clientConfigs);
            IO.info("Server config: Gui enabled: %s", serverConfigs.isEnable_gui());
            IO.info("Server config: Max memory: %dGb", serverConfigs.getMaxMemoryGb());

            if(serverConfigs.isTime_limit_enabled())
                IO.info("Server config: Time limit set to %d seconds", serverConfigs.getTime_limit_seconds());
            else
                IO.info("Server config: no time limit");

            var command = new ArrayList<String>(){{
                add("java.exe"); add("-jar");
                add("server.jar"); add("-l"); add(file.getAbsolutePath());
                add("-c"); add("java -Xmx%dg -cp %s %s %s"
                        .formatted(serverConfigs.getMaxMemoryGb(), classPath, mainClass, clientConfigs));
            }};

            if(serverConfigs.isEnable_gui())
                command.add("-g");

            if(serverConfigs.isTime_limit_enabled())
                command.add("-t"); command.add(Integer.toString(serverConfigs.getTime_limit_seconds()));

            new ProcessBuilder()
                    .directory(IO.G5Dir.toFile())
                    .command(command)
                    .inheritIO()
                    .start()
                    .waitFor();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
