package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.AllowedMentions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class TimeSuggestMessage implements NextSessionListener {
    private final GatewayDiscordClient client;
    @Setter
    private NextSessionManager nextSessionManager;

    @Override
    public void onUpdate(NextSession session) {
        // CAUTION: Calling nextSessionManager.update will result in all the listeners being called again.
        // Without this null check, this code will infinitely loop.
        // Take extra precaution when touching this code!
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
            ZonedDateTime start;
            if (bestStartTime.isEmpty() && bestEndTime.isEmpty()) {
                start = session.getProposedStartTime();
                message = String.format("All players can start whenever, so the start time is %s!",
                        FormatUtils.formatShortDateTime(session.getProposedStartTime()));
            } else if (bestStartTime.isEmpty()) {
                start = session.getProposedStartTime();
                message = String.format("All players can start whenever. As such, I set the start time to %s! However, session must wrap up by %s.",
                        FormatUtils.formatShortDateTime(session.getProposedStartTime()),
                        FormatUtils.formatShortDateTime(bestEndTime.get()));
            } else if (bestEndTime.isEmpty()) {
                start = bestStartTime.get();
                message = String.format("I set the start time to the earliest time for all players, %s!",
                        FormatUtils.formatShortDateTime(bestStartTime.get()));
            } else {
                var bst = bestStartTime.get();
                var bet = bestEndTime.get();
                if (bst.isBefore(bet)) {
                    var duration = Duration.between(bst, bet).toMinutes();
                    if (duration < 120) {
                        start = bst;
                        message = String.format("I set the start time to the earliest time for all players, %s. However, session must end by %s, which would be a short session. You might want to cancel.",
                                FormatUtils.formatShortDateTime(bestStartTime.get()),
                                FormatUtils.formatShortDateTime(bestEndTime.get()));
                    }
                    else {
                        start = bst;
                        message = String.format("I set the start time to the earliest time for all players, %s! However, session must wrap up by %s.",
                                FormatUtils.formatShortDateTime(bestStartTime.get()),
                                FormatUtils.formatShortDateTime(bestEndTime.get()));
                    }
                } else {
                    start = null;
                    message = "There is a conflict, and some players requested schedules don't overlap. I can't recommend a start time.";
                }
            }

            if (start != null) {
                nextSessionManager.update(session.getChannel(), ns -> {
                    ns.setStartTime(start);
                });
            }

            client.getChannelById(session.getChannel())
                    .cast(TextChannel.class)
                    .flatMap(tc ->
                            tc.createMessage(FormatUtils.pingUser(session.getGm()) + " The tallies are in!\n" + message)
                                    .withAllowedMentions(AllowedMentions.builder().allowUser(session.getGm()).build()))
                    .onErrorResume(ex -> {
                        log.error("Unable to display time message", ex);
                        return Mono.empty();
                    })
                    .subscribe();
        }
    }
}
