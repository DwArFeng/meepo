package com.dwarfeng.meepo.handler.daemon;

import com.dwarfeng.subgrade.stack.handler.Handler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LauncherSettingHandler implements Handler {

    @Value("${daemon.launcher.start_execute_delay}")
    private long startExecuteDelay;

    @Value("${daemon.launcher.start_arrival_delay}")
    private long startArrivalDelay;

    public long getStartExecuteDelay() {
        return startExecuteDelay;
    }

    public long getStartArrivalDelay() {
        return startArrivalDelay;
    }

    @Override
    public String toString() {
        return "LauncherSettingHandler{" +
                "startExecuteDelay=" + startExecuteDelay +
                ", startArrivalDelay=" + startArrivalDelay +
                '}';
    }
}
