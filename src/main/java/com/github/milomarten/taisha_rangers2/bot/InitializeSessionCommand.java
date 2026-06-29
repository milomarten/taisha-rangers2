package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.command.response.ReplyResponse;
import com.github.milomarten.taisha_rangers2.state.*;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import com.github.milomarten.taisha_rangers2.util.SessionDateUtil;
import discord4j.common.util.Snowflake;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.rest.util.AllowedMentions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
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
        } else if (party.getPlayerIdentities().isEmpty()) {
            return localizationFactory.createResponse("errors.party.no-members", params.partyName)
                    .ephemeral(true);
        }

        ZonedDateTime proposedStart;
        try {
            proposedStart =
                    params.proposedStart.isEmpty() ?
                            party.getUsualTime().getNextPossibleTime() :
                            SessionDateUtil.parseDatePossibleOptions(params.proposedStart, party.getUsualTime());
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
            return createInitResponse(session, proposedStart);
        }
    }

    public LocalizationFactory.LocalizedDynamicReplyResponse createInitResponse(NextSession session, ZonedDateTime proposedStart) {
        var pingText = session.getPing() == null ? "everyone" : FormatUtils.pingRole(session.getPing());
        return localizationFactory.createComplexResponse((ms, locale) -> {
            var successMsg = ms.getMessage("command.init.response.success",
                    new Object[]{pingText, FormatUtils.formatShortDateTime(proposedStart)}, locale);
            return new ReplyResponse(successMsg)
                    .ephemeral(false)
                    .allowedMentions(AllowedMentions.builder().allowRole(session.getPing()).build())
                    .component(createYesNoButtons(ms, locale));
        });
    }

    private List<Snowflake> checkOOOs(Party party, ZonedDateTime when) {
        var whoOut = new ArrayList<>(oooManager.whoIsOutOn(when.toLocalDate()));
        var relevant = new HashSet<>(Set.of(party.getDm()));
        relevant.addAll(party.getPlayerIdentities().keySet());
        whoOut.retainAll(relevant);
        return whoOut;
    }

    private ActionRow createYesNoButtons(MessageSource ms, Locale locale) {
        return ActionRow.of(
                Button.success("yes", ms.getMessage("command.init.buttons.yes", null, locale)),
                Button.danger("no", ms.getMessage("command.init.buttons.no", null, locale)),
                Button.secondary("maybe-PT24H", ms.getMessage("command.init.buttons.maybeTomorrow", null, locale))
        );
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionIdentityParameters {
        private String partyName;
        private String proposedStart;
    }
}
