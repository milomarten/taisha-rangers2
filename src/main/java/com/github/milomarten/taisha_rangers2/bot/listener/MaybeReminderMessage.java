package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.AllowedMentions;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(GatewayDiscordClient.class)
public class MaybeReminderMessage extends BaseSessionScheduler<MaybeReminderMessage.PlayerId> implements NextSessionListener {
    private final GatewayDiscordClient client;
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
            client.getChannelById(session.getChannel())
                    .cast(TextChannel.class)
                    .flatMap(tc -> {
                        var text = String.format("Hey %s! Just reminding you to register `/yes` or `/no` for session. It's scheduled for %s!",
                                FormatUtils.pingUser(playerId),
                                FormatUtils.formatShortDateTime(session.getProposedStartTime()));
                        return tc.createMessage(text)
                                .withAllowedMentions(AllowedMentions.builder()
                                        .allowUser(playerId)
                                        .build());
                    })
                    .onErrorResume(ex -> {
                        log.error("Unable to send maybe ping", ex);
                        return Mono.empty();
                    })
                    .subscribe();
        }
    }

    protected record PlayerId(Snowflake channel, Snowflake user) {}
}
