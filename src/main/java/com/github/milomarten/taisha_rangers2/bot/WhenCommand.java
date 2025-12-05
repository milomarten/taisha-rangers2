package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.OneNonParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("when")
public class WhenCommand extends AbstractSessionCommand<SessionIdentityParameters> {
    public WhenCommand() {
        super("when");
        setParameterParser(SessionIdentityParameters.parser());
    }

    @Override
    protected CommandResponse doAction(SessionIdentityParameters params, NextSession session) {
        if (session.getStartTime() == null) {
//            return CommandResponse.reply(
//                    String.format("""
//                                        Session is currently scheduled for %s.
//                                        However, I'm still awaiting all player inputs, so that may change.
//                                        %d/%d players have weighed in so far.
//                                        Statuses:
//                                        %s
//                                        """,
//                            FormatUtils.formatShortDateTime(session.getProposedStartTime()),
//                            session.getNumberOfPlayersResponded(),
//                            session.getNumberOfPlayers(),
//                            describeStatuses(session)
//                    ),
//                    true
//            );
            return localizationFactory.createResponse((ms, locale) -> {
                var intro = ms.getMessage("command.when.response-unconfirmed.part.intro",
                        new Object[]{
                                FormatUtils.formatShortDateTime(session.getProposedStartTime()),
                                session.getNumberOfPlayersResponded(),
                                session.getNumberOfPlayers()
                        }, locale);
                var statuses = describeStatuses(session, ms, locale);
                return intro + "\n" + statuses;
            });
        } else {
            return localizationFactory.createResponse("command.when.response-confirmed",
                    FormatUtils.formatShortDateTime(session.getProposedStartTime()));
        }
    }

    private String describeStatuses(NextSession session, MessageSource source, Locale locale) {
        return session.getHydratedPlayerResponses()
                .map(pr -> {
                    if (pr.getState() == PlayerResponse.State.NO) {
                        return "- " + source.getMessage("command.when.response-unconfirmed.part.no",
                                new Object[]{FormatUtils.pingUser(pr.getPlayer())}, locale);
                    } else if (pr.getState() == PlayerResponse.State.MAYBE) {
                        return "- " + source.getMessage("command.when.response-unconfirmed.part.maybe",
                                new Object[]{FormatUtils.pingUser(pr.getPlayer()), FormatUtils.formatShortDateTime(pr.getAfterTime())},
                                locale);
                    } else if (pr.getState() == PlayerResponse.State.NO_RESPONSE) {
                        return "- " + source.getMessage("command.when.response-unconfirmed.part.no-response",
                                new Object[]{FormatUtils.pingUser(pr.getPlayer())},
                                locale);
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
