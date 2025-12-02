package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.bot.SessionAdminParams;
import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.EnumParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.PartyManager;
import com.github.milomarten.taisha_rangers2.state.PartyTime;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Objects;

@Component("set-usual-time")
public class SetUsualTimeCommand extends CommandSpec<SetUsualTimeCommand.Parameters> {
    private final PartyManager partyManager;

    public SetUsualTimeCommand(PartyManager partyManager) {
        super("set-usual-time", "Set the usual time session happens");
        this.partyManager = partyManager;
        setParameterParser(SessionAdminParams.parser(Parameters::new)
                .withParameterField(
                        "party",
                        "The name of the party to update",
                        StringParameter.REQUIRED,
                        Parameters::setPartyName
                )
                .withParameterField(
                        "dayOfWeek",
                        "The day of the week session usually meets",
                        new EnumParameter<>(DayOfWeek.class),
                        Parameters::setDayOfWeek
                )
                .withParameterField(
                        "time",
                        "The time of day session usually meets",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualTime),
                        Parameters::setTime
                )
                .withParameterField(
                        "timezone",
                        "The timezone the time is relative to",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualTimezone),
                        Parameters::setTimezone
                )
        );
        setPermissions(EnumSet.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        return partyManager.updatePartyAndReturn(params.getPartyName(), party -> {
            if (!Objects.equals(party.getDm(), params.getUserId())) {
                return CommandResponse.reply("Only the DM can modify this party", true);
            }

            party.setUsualTime(params.toPartyTime());
            return CommandResponse.reply(
                    String.format("Set the usual time to %ss at %s %s",
                            params.dayOfWeek, params.time, params.timezone),
                    true
            );
        }).orElseGet(() -> CommandResponse.reply("No party with that name!", true));
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionAdminParams {
        private String partyName;
        private DayOfWeek dayOfWeek;
        private LocalTime time;
        private ZoneId timezone;

        public PartyTime toPartyTime() {
            return new PartyTime(dayOfWeek, time, timezone);
        }
    }
}
