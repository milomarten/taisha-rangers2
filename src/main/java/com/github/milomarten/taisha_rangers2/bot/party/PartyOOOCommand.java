package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.OutOfOfficeManager;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.Period;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

@Component("party-ooo")
public class PartyOOOCommand extends LocalizedCommandSpec<PartyOOOCommand.Parameters> {
    private final PartyManager partyManager;
    private final OutOfOfficeManager oooManager;

    public PartyOOOCommand(PartyManager partyManager, OutOfOfficeManager oooManager) {
        super("party-ooo");
        this.partyManager = partyManager;
        this.oooManager = oooManager;

        setParameterParser(PartyIdentityParameters.parser(Parameters::new)
                .withParameterField(
                        "period",
                        IntParameter.builder().defaultValue(2).minValue(1).maxValue(16).build(),
                        Parameters::setPeriod)
        );
    }

    @Override
    protected CommandResponse doAction(Parameters params) {
        var partyMaybe = partyManager.getParty(params.getPartyName());
        if (partyMaybe.isEmpty()) {
            return localizationFactory.createResponse("errors.party.no-match", params.getPartyName());
        }
        var party = partyMaybe.get();
        if (party.getPlayers().isEmpty()) {
            return localizationFactory.createResponse("errors.party.no-players", params.getPartyName());
        }

        var toCheck = new HashSet<>(party.getPlayers());
        toCheck.add(party.getDm());
        var ooos = oooManager.getUpcoming(toCheck, Period.ofWeeks(params.period));
        if (ooos.isEmpty()) {
            return localizationFactory.createResponse("command.party-ooo.response.none", params.period)
                    .ephemeral(true);
        } else {
            return localizationFactory.createResponse((source, locale) -> {
               var prefix = source.getMessage("command.party-ooo.response.some", new Object[]{params.period}, locale);
               return ooos.stream()
                       .map(ooo -> {
                           var item = "- " + FormatUtils.pingUser(ooo.getPlayer());
                           String period;
                           if (Objects.equals(ooo.getStart(), ooo.getEnd())) {
                               period = DateUtil.getPrettyDate(ooo.getStart(), locale);
                           } else {
                               period = DateUtil.getPrettyDate(ooo.getStart(), locale) + " - " + DateUtil.getPrettyDate(ooo.getEnd(), locale);
                           }
                           return item + "(" + period + ")";
                       })
                       .collect(Collectors.joining("\n", prefix + "\n", ""));
            })
                    .ephemeral(true);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyIdentityParameters {
        private int period;
    }
}
