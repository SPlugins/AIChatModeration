package com.ssomar.aichatmoderation.scheduler;
public interface ScheduledTask {

    void cancel();

    boolean isCancelled();

}