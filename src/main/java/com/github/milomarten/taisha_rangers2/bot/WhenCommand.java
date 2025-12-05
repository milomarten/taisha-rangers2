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

import java.time.ZonedDateTime;
import java.util.*;
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
                    var params = new ArrayList<>();
                    params.add(FormatUtils.pingUser(pr.getPlayer()));
                    Optional.ofNullable(pr.getAfterTime()).map(FormatUtils::formatShortDateTime).ifPresent(params::add);
                    Optional.ofNullable(pr.getBeforeTime()).map(FormatUtils::formatShortDateTime).ifPresent(params::add);

                    String key = switch (pr.getState()) {
                        case NO -> "command.when.response-unconfirmed.part.no";
                        case MAYBE -> "command.when.response-unconfirmed.part.maybe";
                        case NO_RESPONSE -> "command.when.response-unconfirmed.part.no-response";
                        case YES -> getYesKey(pr.getAfterTime(), pr.getBeforeTime());
                    };
                    return source.getMessage(key, params.toArray(), locale);
                })
                .map(str -> "- " + str)
                .collect(Collectors.joining("\n"));
    }

    private String getYesKey(ZonedDateTime after, ZonedDateTime before) {
        return String.format("command.when.response-unconfirmed.part.yes-%s-%s",
                after == null ? "whenever" : "start",
                before == null ? "whenever" : "end"
        );
    }
}
