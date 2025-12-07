package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.parameter.EnumParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.Party;
import com.github.milomarten.taisha_rangers2.state.PartyTime;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;

@Component("set-usual-time")
public class SetUsualTimeCommand extends AbstractPartyAdminCommand<SetUsualTimeCommand.Parameters> {
    public SetUsualTimeCommand() {
        super("set-usual-time");
        setParameterParser(PartyIdentityParameters.parser(Parameters::new)
                .withParameterField(
                        "day-of-week",
                        new EnumParameter<>(DayOfWeek.class),
                        Parameters::setDayOfWeek
                )
                .withParameterField(
                        "time",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualTime),
                        Parameters::setTime
                )
                .withParameterField(
                        "timezone",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualTimezone),
                        Parameters::setTimezone
                )
        );
    }

    @Override
    protected CommandResponse doProtectedPartyAction(Party party, Parameters params) {
        party.setUsualTime(params.toPartyTime());
        return localizationFactory.createResponse((source, locale) -> {
            var dayFormatted = DAY_OF_WEEK_FORMATTER
                    .withLocale(locale)
                    .format(params.getDayOfWeek());
            var timeFormatted = DateTimeFormatter
                    .ofLocalizedTime(FormatStyle.SHORT)
                    .withLocale(locale)
                    .format(params.getTime());
            var timezoneFormatted = params.timezone.getDisplayName(TextStyle.SHORT_STANDALONE, locale);

            return source.getMessage("command.set-usual-time.response",
                    new Object[]{
                            dayFormatted,
                            timeFormatted,
                            timezoneFormatted
                    },
                    locale);
        });
    }

    private static final DateTimeFormatter DAY_OF_WEEK_FORMATTER = DateTimeFormatter.ofPattern("EEEE");

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends PartyIdentityParameters {
        private DayOfWeek dayOfWeek;
        private LocalTime time;
        private ZoneId timezone;

        public PartyTime toPartyTime() {
            return new PartyTime(dayOfWeek, time, timezone);
        }
    }
}
