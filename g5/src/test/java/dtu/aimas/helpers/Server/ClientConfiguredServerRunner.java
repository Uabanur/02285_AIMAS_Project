package dtu.aimas.helpers.Server;

import java.util.function.Consumer;

public interface ClientConfiguredServerRunner extends ReadyServerRunner {
    ServerConfiguredServerRunner configureServer(Consumer<ServerConfig> config);
}
