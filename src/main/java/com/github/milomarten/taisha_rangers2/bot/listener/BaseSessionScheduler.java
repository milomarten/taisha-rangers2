package com.github.milomarten.taisha_rangers2.bot.listener;

import discord4j.common.util.Snowflake;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;

@Slf4j
public abstract class BaseSessionScheduler<KEY> {
    @Setter @Autowired private TaskScheduler taskScheduler;
    private final Map<KEY, Context> sessions = new ConcurrentHashMap<>();

    protected void schedule(KEY flake, Runnable task, Instant when) {
        if (sessions.containsKey(flake)) {
            log.info("Task {} already exists. Cancelling...", flake);
            sessions.get(flake).cancel();
        }
        log.info("Scheduling task {} for {} at {}", task, flake, when);
        sessions.put(flake, new Context(when, taskScheduler.schedule(task, when)));
    }

    protected void cancel(KEY flake) {
        if (sessions.containsKey(flake)) {
            sessions.get(flake).cancel();
            sessions.remove(flake);
            log.info("Canceling task {}...", flake);
        }
    }

    protected void cancelIf(Predicate<KEY> predicate) {
        var initSize = sessions.size();
        sessions.entrySet()
            .removeIf(entry -> predicate.test(entry.getKey()));
        log.info("Cancelled {} tasks", initSize - sessions.size());
    }

    protected Instant getScheduledTime(KEY flake) {
        if (sessions.containsKey(flake)) {
            return sessions.get(flake).when();
        }
        return null;
    }

    private record Context(Instant when, ScheduledFuture<?> future) {
        void cancel() {
            future.cancel(false);
        }
    }
}
