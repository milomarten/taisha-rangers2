package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import discord4j.common.util.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DebugPrinter implements NextSessionListener {
    @Override
    public void onLoad(NextSession nextSession) {
        log.info("Loaded: {}", nextSession);
    }

    @Override
    public void onCreate(NextSession session) {
        log.info("Created: {}", session);
    }

    @Override
    public void onUpdate(NextSession session) {
        log.info("Updated: {}", session);
    }

    @Override
    public void onDelete(Snowflake channel) {
        log.info("Deleted: {}", channel);
    }
}
