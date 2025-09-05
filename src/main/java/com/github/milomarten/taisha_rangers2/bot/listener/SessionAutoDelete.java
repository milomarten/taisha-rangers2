package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import discord4j.common.util.Snowflake;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SessionAutoDelete extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    @Setter
    private NextSessionManager nextSessionManager;

    @Override
    public void onLoad(NextSession nextSession) {
        onUpdate(nextSession);
    }

    @Override
    public void onUpdate(NextSession session) {
        var startTime = session.getStartTime();
        if (startTime != null) {
            var autoDeleteAt = startTime.plusHours(8);
            schedule(
                    session.getChannel(),
                    () -> {
                        var worked = nextSessionManager.cancelSession(session.getChannel());
                        if (worked) {
                            log.info("Auto-deleted session {}", session.getChannel());
                        }
                    },
                    autoDeleteAt.toInstant()
            );
        }
    }

    @Override
    public void onDelete(Snowflake channel) {
        cancel(channel);
    }
}
