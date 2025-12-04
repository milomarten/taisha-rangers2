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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
//@ConditionalOnBean(GatewayDiscordClient.class)
public class FarOffSessionMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final GatewayDiscordClient client;
    @Setter
    private NextSessionManager nextSessionManager;
    private final TimingHelper timingHelper;

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
        var pingText = session.getPing() == null ? "everyone" : FormatUtils.pingRole(session.getPing());
        var text = String.format("Hey %s! A session has been scheduled for %s. Let me know if you can join, by typing `/yes` or `/no`!",
                pingText, FormatUtils.formatShortDateTime(session.getProposedStartTime()));

        client.getChannelById(session.getChannel())
                .cast(TextChannel.class)
                .flatMap(tc -> {
                    var msg = tc.createMessage(text);
                    if (session.getPing() != null) {
                        msg = msg.withAllowedMentions(AllowedMentions.builder()
                                .allowRole(session.getPing())
                                .build());
                    }
                    return msg;
                })
                .onErrorResume(ex -> {
                    log.error("Unable to ping players for upcoming session", ex);
                    return Mono.empty();
                })
                .subscribe();
    }
}
