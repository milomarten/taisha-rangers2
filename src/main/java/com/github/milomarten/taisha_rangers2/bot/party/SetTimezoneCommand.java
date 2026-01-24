package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.PlayerManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import discord4j.common.util.Snowflake;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Component("set-timezone")
public class SetTimezoneCommand extends LocalizedCommandSpec<SetTimezoneCommand.Parameters> {
    private final PlayerManager playerManager;

    public SetTimezoneCommand(PlayerManager playerManager) {
        super("set-timezone");
        this.playerManager = playerManager;

        this.setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withParameterField(
                        PojoParameterParser.userId(),
                        Parameters::setUserId
                )
                .withParameterField(
                        "timezone",
                        StringParameter.REQUIRED.map(DateUtil::parseCasualTimezone),
                        Parameters::setTimezone
                )
        );
    }

    @Override
    protected CommandResponse doAction(Parameters params) {
        playerManager.setUsualPlayerTimezone(
                params.userId,
                params.timezone
        );
        return localizationFactory.createResponse((src, locale) -> {
            var start = src.getMessage("command.set-timezone.response", null, locale);
            var nowFormatted = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
                    .localizedBy(locale)
                    .format(ZonedDateTime.now(params.timezone));
            return start + " " + nowFormatted;
        });
    }

    @Data
    public static class Parameters {
        private Snowflake userId;
        private ZoneId timezone;
    }
}
