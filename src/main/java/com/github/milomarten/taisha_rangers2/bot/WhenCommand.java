package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.OneNonParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import org.springframework.stereotype.Component;

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
                        String.format("Session is currently scheduled for %s. However, I'm still awaiting all player inputs, so that may change. %d/%d players have weighed in so far",
                                FormatUtils.formatShortDateTime(session.getProposedStartTime()),
                                session.getNumberOfPlayersResponded(),
                                session.getNumberOfPlayers()),
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
}
