package dtu.aimas.helpers.Server;

public interface LevelConfiguredServerRunner extends ReadyServerRunner {
    ClientConfiguredServerRunner clientConfigs(String configs);
}
