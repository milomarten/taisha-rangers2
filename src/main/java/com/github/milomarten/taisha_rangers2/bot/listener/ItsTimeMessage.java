package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ItsTimeMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final GatewayDiscordClient client;
    @Setter private NextSessionManager nextSessionManager;

    @Override
    public void onLoad(NextSession nextSession) {
        onUpdate(nextSession);
    }

    @Override
    public void onUpdate(NextSession session) {
        if (session.getStartTime() != null) {
            var newTime = session.getStartTime().toInstant();
            var oldTime = super.getScheduledTime(session.getChannel());
            if (oldTime == null || !oldTime.equals(newTime)) {
                schedule(
                        session.getChannel(),
                        () -> nextSessionManager.getNextSession(session.getChannel())
                                .ifPresent(this::pingToBegin),
                        newTime
                );
            }
        }
    }

    @Override
    public void onDelete(Snowflake channel) {
        cancel(channel);
    }

    private void pingToBegin(NextSession session) {
        var message = String.format(
                "%s %s! It's time for session!",
                FormatUtils.pingUser(session.getGm()),
                FormatUtils.pingRole(session.getPing())
        );

        client.getChannelById(session.getChannel())
                .cast(TextChannel.class)
                .flatMap(tc -> tc.createMessage(message))
                .onErrorResume(ex -> {
                    log.error("Unable to announce session", ex);
                    return Mono.empty();
                })
                .subscribe();
    }
}
