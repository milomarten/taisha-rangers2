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

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(prefix = "reminder", value = "enabled")
public class MaybeReminderMessage extends BaseSessionScheduler<MaybeReminderMessage.PlayerId> implements NextSessionListener {
    private final LocalizedDiscordService client;
    @Setter private NextSessionManager nextSessionManager;

    @Override
    public void onLoad(NextSession nextSession) {
        nextSession.getPlayerResponses()
                .forEach((playerId, playerResponse) -> {
                    if (playerResponse.getState() == PlayerResponse.State.MAYBE) {
                        schedule(
                                new PlayerId(nextSession.getChannel(), playerId),
                                () -> nextSessionManager.getNextSession(nextSession.getChannel())
                                        .ifPresent(ns -> pingPlayerIfNecessary(ns, playerId)),
                                playerResponse.getAfterTime().toInstant()
                        );
                    }
                });
    }

    @Override
    public void onUpdate(NextSession session) {
        session.getPlayerResponses()
            .forEach((snowflake, pr) -> {
                var playerId = new PlayerId(session.getChannel(), pr.getPlayer());
                var previousWhenTime = super.getScheduledTime(playerId);
                if (pr.getState() == PlayerResponse.State.MAYBE) {
                    var newWhenTime = pr.getAfterTime().toInstant();
                    if (!newWhenTime.equals(previousWhenTime)) {
                        schedule(
                                playerId,
                                () -> nextSessionManager.getNextSession(session.getChannel())
                                        .ifPresent(ns -> pingPlayerIfNecessary(ns, pr.getPlayer())),
                                newWhenTime
                        );
                    }
                } else if (previousWhenTime != null) {
                    // Previously said maybe but now says yes/no, so cancel the job
                    cancel(playerId);
                }
            });
    }

    @Override
    public void onDelete(Snowflake channel) {
        cancelIf(pid -> channel.equals(pid.channel));
    }

    private void pingPlayerIfNecessary(NextSession session, Snowflake playerId) {
        var playerResponse = session.getPlayerResponses().get(playerId);
        if (playerResponse != null && playerResponse.getState() == PlayerResponse.State.MAYBE) {
            client.sendLocalizedMessage(
                    session.getChannel(),
                    session.getLocale(),
                    LocalizedDiscordService.withAllowedUserMention(playerId),
                    "job.maybe-reminder",
                    FormatUtils.pingUser(playerId),
                    FormatUtils.formatShortDateTime(session.getProposedStartTime())
            ).subscribe();
        }
    }

    protected record PlayerId(Snowflake channel, Snowflake user) {}
}
