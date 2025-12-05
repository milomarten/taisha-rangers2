package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.*;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.rest.util.AllowedMentions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component("init")
public class InitializeSessionCommand extends LocalizedCommandSpec<InitializeSessionCommand.Parameters> {
    private final NextSessionManager manager;
    private final PartyManager partyManager;
    private final OutOfOfficeManager oooManager;
    private final TimingHelper timingHelper;

    public InitializeSessionCommand(NextSessionManager manager, PartyManager partyManager, OutOfOfficeManager oooManager, TimingHelper timingHelper) {
        super("init");
        this.manager = manager;
        this.partyManager = partyManager;
        this.oooManager = oooManager;
        this.timingHelper = timingHelper;
        setParameterParser(SessionIdentityParameters.parser(Parameters::new)
                .withParameterField(
                        "party",
                        StringParameter.REQUIRED,
                        Parameters::setPartyName
                )
                .withParameterField(
                        "proposed-start-time",
                        StringParameter.DEFAULT_EMPTY_STRING,
                        Parameters::setProposedStart
                )
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(InitializeSessionCommand.Parameters params) {
        var partyMaybe = partyManager.getParty(params.partyName);
        if (partyMaybe.isEmpty()) {
            return localizationFactory.createResponse("errors.party.no-match", params.partyName)
                    .ephemeral(true);
        }
        var party = partyMaybe.get();
        if (!Objects.equals(params.getUserId(), party.getDm())) {
            return localizationFactory.createResponse("errors.party.no-access", params.partyName)
                    .ephemeral(true);
        } else if (party.getPlayers().isEmpty()) {
            return localizationFactory.createResponse("errors.party.no-members", params.partyName)
                    .ephemeral(true);
        }

        ZonedDateTime proposedStart;
        try {
            proposedStart =
                    params.proposedStart.isEmpty() ?
                            party.getUsualTime().getNextPossibleTime() :
                            parseTimestampFromStringAndOptionalContext(params.proposedStart, party.getUsualTime());
            Objects.requireNonNull(proposedStart); // solely to pop down into the catch block.
        } catch (RuntimeException e) {
            return localizationFactory.createResponse("command.init.error.unable-to-deduce-time")
                    .ephemeral(true);
        }

        var ooos = checkOOOs(party, proposedStart);
        if (!ooos.isEmpty()) {
            var asPings = ooos.stream().map(FormatUtils::pingUser).collect(Collectors.joining(" "));
            return localizationFactory.createResponse("command.init.error.ooo-present", asPings)
                    .ephemeral(true);
        }

        var session = manager.createSession(
                params.getChannelId(),
                party,
                proposedStart
        );

        if (timingHelper.isFarOffSession(session)) {
            return localizationFactory.createResponse("command.init.response.success-far-off", FormatUtils.formatShortDateTime(proposedStart))
                    .ephemeral(true);
        } else {
            var pingText = session.getPing() == null ? "everyone" : FormatUtils.pingRole(session.getPing());
            return localizationFactory.createResponse("command.init.response.success", pingText, FormatUtils.formatShortDateTime(proposedStart))
                    .ephemeral(false)
                    .allowedMentions(AllowedMentions.builder().allowRole(session.getPing()).build());
        }
    }

    private ZonedDateTime parseTimestampFromStringAndOptionalContext(String value, PartyTime usualTime) {
        try {
            return DateUtil.parseCasualDateTime(value, usualTime);
        } catch (RuntimeException e) {
            return parseTimeFromStringAndContext(value, usualTime);
        }
    }

    private ZonedDateTime parseTimeFromStringAndContext(String time, PartyTime context) {
        var timeParsed = DateUtil.parseCasualTime(time);
        if (timeParsed == null) { return null; }
        return context.getNextPossibleTime()
                .with(timeParsed);
    }

    private List<Snowflake> checkOOOs(Party party, ZonedDateTime when) {
        var whoOut = new ArrayList<>(oooManager.whoIsOutOn(when.toLocalDate()));
        whoOut.retainAll(party.getPlayers());
        return whoOut;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionIdentityParameters {
        private String partyName;
        private String proposedStart;
    }
}
