package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.config.LocalizedDiscordService;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnBooleanProperty(prefix = "reminder", value = "enabled")
public class FarOffSessionMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final LocalizedDiscordService client;
    @Setter
    private NextSessionManager nextSessionManager;

    @Override
    public void onLoad(NextSession nextSession) {
        onCreate(nextSession);
    }

    @Override
    public void onCreate(NextSession session) {
        if (timingHelper.isFarOffSession(session)) {
            schedule(
                    session.getChannel(),
                    () -> nextSessionManager.getNextSession(session.getChannel()).ifPresent(this::pingPlayersForUpcomingSession),
                    timingHelper.getAnnouncementTime(session).toInstant()
            );
        }
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

    private void pingPlayersForUpcomingSession(NextSession session) {
        client.sendLocalizedMessage(
                session.getChannel(),
                session.getLocale(),
                LocalizedDiscordService.withAllowedRoleMention(session.getPing()),
                "job.far-off-session",
                session.getPing() != null ? FormatUtils.pingRole(session.getPing()) : "everyone",
                FormatUtils.formatShortDateTime(session.getProposedStartTime())
        ).subscribe();
    }
}
