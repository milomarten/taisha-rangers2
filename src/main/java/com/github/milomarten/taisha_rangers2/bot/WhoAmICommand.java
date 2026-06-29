package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.*;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Component("whoami")
public class WhoAmICommand extends LocalizedCommandSpec<SessionIdentityParameters> {
    private final PlayerManager playerManager;
    private final PartyManager partyManager;
    private final NextSessionManager nextSessionManager;

    private final LocalizationFactory localizationFactory;

    public WhoAmICommand(PlayerManager playerManager, PartyManager partyManager, NextSessionManager nextSessionManager, LocalizationFactory localizationFactory) {
        super("whoami");
        this.playerManager = playerManager;
        this.partyManager = partyManager;
        this.nextSessionManager = nextSessionManager;
        this.localizationFactory = localizationFactory;

        setParameterParser(SessionIdentityParameters.parser());
    }

    @Override
    protected CommandResponse doAction(SessionIdentityParameters params) {
        var output = new StringJoiner("\n");
        output.add("Hi, " + FormatUtils.pingUser(params.getUserId()) + "!");
        var player = playerManager.getPlayer(params.getUserId());

        if (player == null) {
            output.add("I don't know anything about you, unfortunately.");
            return CommandResponse.reply(output.toString(), false);
        }

        return localizationFactory.createComplexResponse((ms, locale) -> {
            if (player.getUsualTimezone() != null) {
                output.add("Your usual timezone is " + player.getUsualTimezone());
            }

            if (player.getOutOfOffices() != null && !player.getOutOfOffices().isEmpty()) {
                output.add(String.format("I see you have %d OOOs registered: %s",
                        player.getOutOfOffices().size(), formatOutOfOffices(player.getOutOfOffices(), locale)));
            }

            var ins = partiesIn(params.getUserId());
            var running = partiesGMing(params.getUserId());

            if (!ins.isEmpty()) {
                output.add("You have characters in the following campaigns:");
                for (var in : ins) {
                    var ns = findNextSession(in.getName());
                    var charName = in.getPlayerIdentities().get(params.getUserId()).getName();
                    if (ns == null) {
                        output.add(String.format("- `%s` (as %s)", in.getName(), charName));
                    } else if (ns.getStartTime() != null) {
                        output.add(String.format("- `%s` (as %s) (%s in %s)", in.getName(), charName,
                                FormatUtils.formatShortDateTime(ns.getStartTime()), FormatUtils.mentionChannel(ns.getChannel())));
                    } else {
                        output.add(String.format("- `%s` (as %s) (scheduled %s in %s)", in.getName(), charName,
                                FormatUtils.formatShortDateTime(ns.getProposedStartTime()), FormatUtils.mentionChannel(ns.getChannel())));
                    }
                }
            }

            if (!running.isEmpty()) {
                if (!ins.isEmpty()) {
                    output.add("");
                }
                output.add("You are running the following campaigns:");
                for (var in : running) {
                    var ns = findNextSession(in.getName());
                    if (ns == null) {
                        output.add(String.format("- `%s` (not scheduled)", in.getName()));
                    } else if (ns.getStartTime() != null) {
                        output.add(String.format("- `%s` (%s in %s)", in.getName(),
                                FormatUtils.formatShortDateTime(ns.getStartTime()), FormatUtils.mentionChannel(ns.getChannel())));
                    } else {
                        output.add(String.format("- `%s` (scheduled %s in %s)", in.getName(),
                                FormatUtils.formatShortDateTime(ns.getProposedStartTime()), FormatUtils.mentionChannel(ns.getChannel())));
                    }
                }
            }

            return CommandResponse.reply(output.toString(), false);
        });
    }

    private String formatOutOfOffices(List<OutOfOffice> ooos, Locale locale) {
        return ooos.stream()
                .map(ooo -> {
                    var days = ChronoUnit.DAYS.between(ooo.getStart(), ooo.getEnd().plusDays(1));
                    return DateUtil.getPrettyDate(ooo.getStart(), locale) + "(" + days + (days == 1 ? "day)" : " days)");
                })
                .collect(Collectors.joining(", "));
    }

    private List<Party> partiesIn(Snowflake who) {
        return partyManager.getParties()
                .stream()
                .filter(p -> p.getPlayerIdentities().containsKey(who))
                .toList();
    }

    private List<Party> partiesGMing(Snowflake who) {
        return partyManager.getParties()
                .stream()
                .filter(p -> p.getDm().equals(who))
                .toList();
    }

    private NextSession findNextSession(String name) {
        return nextSessionManager.getNextSessions()
                .stream()
                .filter(ns -> Objects.equals(name, ns.getParty().getName()))
                .findFirst()
                .orElse(null);
    }
}
