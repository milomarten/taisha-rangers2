package com.github.milomarten.taisha_rangers2.bot.listener;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;

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
            log.info("{} Task {} already exists. Cancelling...", getClass().getSimpleName(), flake);
            sessions.get(flake).cancel();
        }

        if (Instant.now().isAfter(when)) {
            log.info("{} Task {} is scheduled for the past.", getClass().getSimpleName(), flake);
        }

        log.info("Scheduling {} task for {} at {}", getClass().getSimpleName(), flake, when);
        Runnable wrappedTask = () -> {
            log.info("Starting {} task {}", getClass().getSimpleName(), flake);
            task.run();
            sessions.remove(flake);
        };
        sessions.put(flake, new Context(when, taskScheduler.schedule(wrappedTask, when)));
    }

    protected void cancel(KEY flake) {
        if (sessions.containsKey(flake)) {
            sessions.get(flake).cancel();
            sessions.remove(flake);
            log.info("Canceling {} task {}...", getClass().getSimpleName(), flake);
        }
    }

    protected void cancelIf(Predicate<KEY> predicate) {
        var initSize = sessions.size();
        sessions.entrySet()
            .removeIf(entry -> {
                if (predicate.test(entry.getKey())) {
                    entry.getValue().cancel();
                    return true;
                }
                return false;
            });
        log.info("Cancelled {} {} tasks", initSize - sessions.size(), getClass().getSimpleName());
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
