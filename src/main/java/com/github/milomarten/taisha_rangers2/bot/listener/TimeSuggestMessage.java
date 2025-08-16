package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class TimeSuggestMessage implements NextSessionListener {
    private final GatewayDiscordClient client;

    @Override
    public void onUpdate(NextSession session) {
        if (session.getStartTime() == null && session.allPlayersRespondedYes()) {
            var responses = session.getPlayerResponses().values();

            var bestStartTime = responses.stream()
                    .map(PlayerResponse::getAfterTime)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder());
            var bestEndTime = responses.stream()
                    .map(PlayerResponse::getBeforeTime)
                    .filter(Objects::nonNull)
                    .min(Comparator.naturalOrder());

            String message;
            if (bestStartTime.isEmpty() && bestEndTime.isEmpty()) {
                message = String.format("All players can start whenever. As such, I recommend the scheduled start time, %s!",
                        FormatUtils.formatShortDateTime(session.getProposedStartTime()));
            } else if (bestStartTime.isEmpty()) {
                message = String.format("All players can start whenever. As such, I recommend the scheduled start time, %s! However, session must end by %s.",
                        FormatUtils.formatShortDateTime(session.getProposedStartTime()),
                        FormatUtils.formatShortDateTime(bestEndTime.get()));
            } else if (bestEndTime.isEmpty()) {
                message = String.format("All players should be able to start by %s!",
                        FormatUtils.formatShortDateTime(bestStartTime.get()));
            } else {
                message = String.format("All players should be able to start by %s! Also, session must end by %s.",
                        FormatUtils.formatShortDateTime(bestStartTime.get()),
                        FormatUtils.formatShortDateTime(bestEndTime.get()));
            }

            client.getChannelById(session.getChannel())
                    .cast(TextChannel.class)
                    .flatMap(tc -> tc.createMessage(FormatUtils.pingUser(session.getGm()) + " The tallies are in! " + message))
                    .onErrorResume(ex -> {
                        log.error("Unable to display time message", ex);
                        return Mono.empty();
                    })
                    .subscribe();
        }
    }
}
