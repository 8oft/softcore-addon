package com.softcore.addon.utils.timer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MsTimer {
    public static final ScheduledExecutorService s = new ScheduledThreadPoolExecutor(2);

    public static void schedule(Runnable task, long delayMs) {
        s.schedule(task, delayMs, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
