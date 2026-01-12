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

import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBooleanProperty(prefix = "reminder", value = "enabled")
public class DayOfReminderMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final LocalizedDiscordService client;
    @Setter private NextSessionManager nextSessionManager;

    @Override
    public void onLoad(NextSession nextSession) {
        onUpdate(nextSession);
    }

    @Override
    public void onUpdate(NextSession session) {
        if (session.getStartTime() != null) {
            var firstPingDate = timingHelper.getDayOfPingTime(session);
            var now = ZonedDateTime.now();
            if (firstPingDate.isAfter(now)) {
                schedule(session.getChannel(),
                        () -> nextSessionManager.getNextSession(session.getChannel())
                                .ifPresent(this::doPlayerPingIfNecessary),
                        firstPingDate.toInstant());
            } else {
                log.info("Too close to scheduled session time, so not scheduling a ping");
            }
        }
    }

    @Override
    public void onDelete(Snowflake channel) {
        cancel(channel);
    }

    private void doPlayerPingIfNecessary(NextSession session) {
            client.sendLocalizedMessage(
                    session.getChannel(),
                    session.getLocale(),
                    LocalizedDiscordService.withAllowedRoleMention(session.getPing()),
                    "job.day-of-ping",
                    FormatUtils.pingRole(session.getPing()),
                    FormatUtils.formatShortDateTime(session.getStartTime())
            ).subscribe();
    }
}
