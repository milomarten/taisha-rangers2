package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.bot.TimingHelper;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.AllowedMentions;
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
    @Setter private TimingHelper timingHelper;

    @Override
    public void onLoad(NextSession nextSession) {
        onUpdate(nextSession);
    }

    @Override
    public void onUpdate(NextSession session) {
        if (session.getStartTime() != null) {
            var newTime = timingHelper.getSessionStartReminderTime(session).toInstant();
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
                "%s %s! Session starts %s!",
                FormatUtils.pingUser(session.getGm()),
                FormatUtils.pingRole(session.getPing()),
                FormatUtils.formatRelativeTime(session.getStartTime())
        );

        client.getChannelById(session.getChannel())
                .cast(TextChannel.class)
                .flatMap(tc ->
                        tc.createMessage(message)
                                .withAllowedMentions(AllowedMentions.builder()
                                        .allowRole(session.getPing())
                                        .allowUser(session.getGm())
                                .build()))
                .onErrorResume(ex -> {
                    log.error("Unable to announce session", ex);
                    return Mono.empty();
                })
                .subscribe();
    }
}
