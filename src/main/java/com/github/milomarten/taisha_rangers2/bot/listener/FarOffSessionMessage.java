package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.bot.InitSessionMessageService;
import com.github.milomarten.taisha_rangers2.config.LocalizedDiscordService;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
@ConditionalOnBooleanProperty(prefix = "reminder", value = "enabled")
public class FarOffSessionMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final LocalizedDiscordService client;
    private final InitSessionMessageService initSessionMessageService;

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
        initSessionMessageService.createInitResponse(session, session.getProposedStartTime())
                .resolve(session.getLocale())
                .send(client.getGateway(), session.getChannel())
                .onErrorResume(ex -> {
                    log.error("Error sending init. Eating message", ex);
                    return Mono.empty();
                })
                .subscribe();
    }
}
