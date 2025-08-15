package com.github.milomarten.taisha_rangers2.bot.listener;

import discord4j.common.util.Snowflake;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public abstract class BaseSessionScheduler<KEY> {
    @Setter @Autowired private TaskScheduler taskScheduler;
    private final Map<KEY, Context> sessions = new ConcurrentHashMap<>();

    protected void schedule(KEY flake, Runnable task, Instant when) {
        if (sessions.containsKey(flake)) {
            sessions.get(flake).cancel();
        }
        sessions.put(flake, new Context(when, taskScheduler.schedule(task, when)));
    }

    protected void cancel(KEY flake) {
        if (sessions.containsKey(flake)) {
            sessions.get(flake).cancel();
            sessions.remove(flake);
        }
    }

    private record Context(Instant when, ScheduledFuture<?> future) {
        void cancel() {
            future.cancel(false);
        }
    }
}
