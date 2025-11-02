package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.SnowflakeParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.state.OutOfOfficeManager;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.rest.util.AllowedMentions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component("init")
public class InitializeSessionCommand extends CommandSpec<InitializeSessionCommand.Parameters> {
    private final NextSessionManager manager;
    private final PartyManager partyManager;
    private final OutOfOfficeManager oooManager;

    public InitializeSessionCommand(NextSessionManager manager, PartyManager partyManager, OutOfOfficeManager oooManager) {
        super("init", "Create an upcoming session");
        this.manager = manager;
        this.partyManager = partyManager;
        this.oooManager = oooManager;
        setParameterParser(SessionAdminParams.parser(Parameters::new)
                .withParameterField(
                        "party",
                        "The party in this session",
                        StringParameter.REQUIRED,
                        Parameters::setPartyName
                )
                .withParameterField(
                        "proposed-start-time",
                        "The proposed start time for session.",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualDateTime),
                        Parameters::setProposedStart
                )
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(InitializeSessionCommand.Parameters params) {
        var partyMaybe = partyManager.getParty(params.partyName);
        if (partyMaybe.isEmpty()) {
            return CommandResponse.reply(
                    String.format("No party with name %s in my system. If it's a new party, you have to make it using another command", params.partyName),
                    true
            );
        }
        var party = partyMaybe.get();
        if (!Objects.equals(params.getUserId(), party.getDm())) {
            return CommandResponse.reply(
                    "Only the DM can create a session for this party",
                    true
            );
        } else if (party.getPlayers().isEmpty()) {
            return CommandResponse.reply(
                    "Party has no players! Please add at least one player.",
                    true
            );
        }
        var ooos = checkOOOs(party, params.proposedStart);
        if (!ooos.isEmpty()) {
            var asPings = ooos.stream().map(FormatUtils::pingUser).collect(Collectors.joining(" "));
            return CommandResponse.reply(
                    String.format("Can't have session that day, players are OOO: %s", asPings),
                    true
            );
        }

        var session = manager.createSession(
                params.getChannelId(),
                party,
                params.proposedStart
        );

        if (session.isFarOffSession()) {
            return CommandResponse.reply(
                    String.format("Scheduled a session for %s. It's a ways off, so I'll announce it closer to time", FormatUtils.formatShortDateTime(params.proposedStart)),
                    true
            );
        } else {
            var pingText = session.getPing() == null ? "everyone" : FormatUtils.pingRole(session.getPing());
            var text = String.format("Hey %s! A session has been scheduled for %s. Let me know if you can join, by typing `/yes` or `/no`!",
                    pingText, FormatUtils.formatShortDateTime(params.proposedStart));
            return CommandResponse.reply(text, false)
                    .allowedMentions(AllowedMentions.builder().allowRole(session.getPing()).build());
        }
    }

    private List<Snowflake> checkOOOs(Party party, ZonedDateTime when) {
        var whoOut = new ArrayList<>(oooManager.whoIsOutOn(when.toLocalDate()));
        whoOut.retainAll(party.getPlayers());
        return whoOut;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionAdminParams {
        private String partyName;
        private ZonedDateTime proposedStart;
    }
}
