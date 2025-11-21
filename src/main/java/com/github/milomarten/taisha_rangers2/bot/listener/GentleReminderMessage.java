package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.bot.TimingHelper;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GentleReminderMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final GatewayDiscordClient client;
    @Setter private NextSessionManager nextSessionManager;
    private final TimingHelper timingHelper;

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
                .map(pr -> FormatUtils.pingUser(pr.getPlayer()))
                .toList();
        if (!noResponsePlayers.isEmpty()) {
            var ping = String.join(", ", noResponsePlayers);
            var message = String.format("Hey %s! Don't forget to send `/yes` or `/no` if you can attend session on %s! Thank you!",
                    ping, FormatUtils.formatShortDateTime(session.getProposedStartTime()));
            this.client.getChannelById(session.getChannel())
                    .cast(TextChannel.class)
                    .flatMap(tc -> tc.createMessage(message))
                    .onErrorResume(ex -> {
                        log.error("Unable to send reminder ping", ex);
                        return Mono.empty();
                    })
                    .subscribe();
        }
    }
}
