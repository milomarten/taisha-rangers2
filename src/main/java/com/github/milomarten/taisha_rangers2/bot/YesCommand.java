package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component("yes")
public class YesCommand extends AbstractSessionPlayerCommand<YesCommand.Parameters> {
    public YesCommand() {
        super("yes");

        this.setParameterParser(SessionIdentityParameters.parser(Parameters::new)
                .withParameterField(
                        "after-time",
                        StringParameter.DEFAULT_EMPTY_STRING.map(DateUtil::parseCasualTime),
                        Parameters::setStartTime
                )
                .withParameterField(
                        "before-time",
                        StringParameter.DEFAULT_EMPTY_STRING.map(DateUtil::parseCasualTime),
                        Parameters::setEndTime
                )
                .withParameterField(
                        "timezone",
                        StringParameter.DEFAULT_EMPTY_STRING
                                .map(DateUtil::parseCasualTimezone),
                        Parameters::setTimezone
                )
        );
    }

    @Override
    protected CommandResponse doPlayerAction(Parameters params, NextSession session, PlayerResponse pr) {
        var startTime = params.startTime == null ? null : computeContextualTime(session.getProposedStartTime(), params.startTime, params.timezone);
        var endTime = params.endTime == null ? null : computeContextualTime(session.getProposedStartTime(), params.endTime, params.timezone);
        pr.yes(startTime, endTime);
        return getResponseString(params.getUsername(), startTime, endTime)
                .ephemeral(false);
    }

    static ZonedDateTime computeContextualTime(
            ZonedDateTime origin,
            LocalTime time,
            ZoneId timezone
    ) {
        var originInTheRightTimezone = origin.withZoneSameInstant(timezone);
        var originStartTime = originInTheRightTimezone.toLocalTime();
        if (time.isAfter(originStartTime)) {
            return originInTheRightTimezone.with(time);
        } else if (time.equals(originStartTime)) {
            return originInTheRightTimezone;
        } else {
            var hoursBefore = Duration.between(time, originStartTime).toHours();
            if (hoursBefore <= 8) {
                return originInTheRightTimezone.with(time);
            } else {
                return originInTheRightTimezone.plusDays(1).with(time);
            }
        }
    }

    private LocalizationFactory.LocalizedReplyResponse getResponseString(String username, ZonedDateTime start, ZonedDateTime end) {
        if (start == null && end == null) {
            return localizationFactory.createResponse("command.yes.response.whenever.whenever", username);
        } else if (start == null) {
            return localizationFactory.createResponse("command.yes.response.whenever.end", username, FormatUtils.formatShortTime(end));
        } else if (end == null) {
            return localizationFactory.createResponse("command.yes.response.start.whenever", username, FormatUtils.formatShortTime(start));
        } else {
            return localizationFactory.createResponse("command.yes.response.start.end", username,
                    FormatUtils.formatShortTime(start), FormatUtils.formatShortTime(end));
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionIdentityParameters {
        LocalTime startTime;
        LocalTime endTime;
        ZoneId timezone;
    }
}
