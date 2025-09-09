package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.OneNonParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component("when")
public class WhenCommand extends CommandSpec<Snowflake> {
    private final NextSessionManager nextSessionManager;

    public WhenCommand(NextSessionManager nextSessionManager) {
        super("when", "Remind yourself when next session is");
        this.nextSessionManager = nextSessionManager;

        setParameterParser(
                new OneNonParameterParser<>(e -> e.getInteraction().getChannelId())
        );
    }

    @Override
    public CommandResponse doAction(Snowflake params) {
        var sessionMaybe = nextSessionManager.getNextSession(params);
        if (sessionMaybe.isPresent()) {
            var session = sessionMaybe.get();
            if (session.getStartTime() == null) {
                return CommandResponse.reply(
                        String.format("""
                                        Session is currently scheduled for %s.
                                        However, I'm still awaiting all player inputs, so that may change.
                                        %d/%d players have weighed in so far.
                                        Statuses:
                                        %s
                                        """,
                                FormatUtils.formatShortDateTime(session.getProposedStartTime()),
                                session.getNumberOfPlayersResponded(),
                                session.getNumberOfPlayers(),
                                describeStatuses(session)
                                ),
                        true
                );
            } else {
                return CommandResponse.reply(
                        String.format("Session is currently scheduled for %s.",
                                FormatUtils.formatShortDateTime(session.getStartTime())),
                        true
                );
            }
        } else {
            return CommandResponse.reply("There's currently no session scheduled.", true);
        }
    }

    private String describeStatuses(NextSession session) {
        return session.getPlayerResponses()
                .values()
                .stream()
                .map(pr -> {
                    if (pr.getState() == PlayerResponse.State.NO) {
                        return String.format("- %s: Can NOT Join", FormatUtils.pingUser(pr.getPlayer()));
                    } else if (pr.getState() == PlayerResponse.State.MAYBE) {
                        return String.format("- %s: Can MAYBE Join (reminding at %s)",
                                FormatUtils.pingUser(pr.getPlayer()),
                                FormatUtils.formatShortDateTime(pr.getAfterTime())
                        );
                    } else if (pr.getState() == PlayerResponse.State.NO_RESPONSE) {
                        return String.format("- %s: No Response Yet", FormatUtils.pingUser(pr.getPlayer()));
                    } else {
                        return String.format("- %s: Can Join (after: %s, before: %s)",
                                FormatUtils.pingUser(pr.getPlayer()),
                                pr.getAfterTime() == null ? "any time" : FormatUtils.formatShortTime(pr.getAfterTime()),
                                pr.getBeforeTime() == null ? "any time" : FormatUtils.formatShortTime(pr.getBeforeTime())
                                );
                    }
                })
                .collect(Collectors.joining("\n"));
    }
}
