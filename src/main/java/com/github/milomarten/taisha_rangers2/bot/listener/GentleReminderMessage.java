package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.config.LocalizedDiscordService;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBooleanProperty(prefix = "reminder", value = "enabled")
public class GentleReminderMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final LocalizedDiscordService client;
    @Setter private NextSessionManager nextSessionManager;

    @Override
    public void onLoad(NextSession nextSession) {
        // One downside is that the bot won't be able to "catch up" on the
        // scheduled messages if it was down. GM's responsibility to do it instead.
        onCreate(nextSession);
    }

    @Override
    public void onCreate(NextSession session) {
        var firstPingDate = timingHelper.getGentleReminderTime(session);
        var now = ZonedDateTime.now();
        if (firstPingDate.isAfter(now)) {
            schedule(session.getChannel(),
                    () -> nextSessionManager.getNextSession(session.getChannel()).ifPresent(this::doPlayerPingIfNecessary),
                    firstPingDate.toInstant());
        } else {
            log.info("Too close to scheduled session time, so not scheduling a ping");
        }
    }

    @Override
    public void onUpdate(NextSession session) {
        if (session.allPlayersResponded()) {
            cancel(session.getChannel());
        }
    }

    @Override
    public void onDelete(Snowflake channel) {
        cancel(channel);
    }

    private void doPlayerPingIfNecessary(NextSession session) {
        var noResponsePlayers = session.getHydratedPlayerResponses()
                .filter(pr -> pr.getState() == PlayerResponse.State.NO_RESPONSE)
                .map(PlayerResponse::getPlayer)
                .toList();
        if (!noResponsePlayers.isEmpty()) {
            var rawPing = noResponsePlayers.stream()
                    .map(FormatUtils::pingUser)
                    .collect(Collectors.joining(", "));
            client.sendLocalizedMessage(
                    session.getChannel(),
                    session.getLocale(),
                    LocalizedDiscordService.withAllowedUserMention(noResponsePlayers),
                    "job.gentle-reminder",
                    rawPing,
                    FormatUtils.formatShortDateTime(session.getProposedStartTime())
            ).subscribe();
        }
    }
}
