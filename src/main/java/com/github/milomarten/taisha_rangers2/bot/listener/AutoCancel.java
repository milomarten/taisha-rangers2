package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.bot.SessionAdminParams;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import discord4j.common.util.Snowflake;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AutoCancel extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private NextSessionManager nextSessionManager;

    @Override
    public void onLoad(NextSession nextSession) {
        if (nextSession.getStartTime() == null) {
            onCreate(nextSession);
        }
    }

    @Override
    public void onCreate(NextSession session) {
        schedule(
                session.getChannel(),
                () -> nextSessionManager.getNextSession(session.getChannel())
                        .ifPresent(this::cancelSessionIfNecessary),
                session.getProposedStartTime().toInstant()
        );
    }

    @Override
    public void onUpdate(NextSession session) {
        if (session.getStartTime() != null) {
            cancel(session.getChannel());
        }
    }

    @Override
    public void onDelete(Snowflake channel) {
        cancel(channel);
    }

    private void cancelSessionIfNecessary(NextSession session) {
        if (session.getStartTime() == null) {
            log.info("Auto-canceling session {}, since no start time was ever provided.", session.getChannel());
            nextSessionManager.cancelSession(new SessionAdminParams(
                    session.getGm(),
                    session.getChannel()
            ));
        }
    }
}
