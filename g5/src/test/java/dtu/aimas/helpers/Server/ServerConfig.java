package dtu.aimas.helpers.Server;

import lombok.Getter;

@Getter
public class ServerConfig {
    private boolean enable_gui = true;
    private boolean time_limit_enabled = false;
    private int time_limit_seconds = 0;
    private int maxMemoryGb = 8;

    public void disableGui(){
        enable_gui = false;
    }

    public void timeLimitSeconds(int time_limit_seconds){
        this.time_limit_enabled = true;
        this.time_limit_seconds = time_limit_seconds;
    }

    public void maxMemoryGb(int gb){
        this.maxMemoryGb = gb;
    }
}
