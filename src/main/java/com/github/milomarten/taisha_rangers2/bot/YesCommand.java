package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component("yes")
public class YesCommand extends CommandSpec<YesCommand.Parameters> {
    private final NextSessionManager nextSessionManager;

    public YesCommand(NextSessionManager nextSessionManager) {
        super("yes", "Indicate that you can make it to session");
        this.nextSessionManager = nextSessionManager;

        this.setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withInteractionField(PojoParameterParser.user(Parameters::setUser))
                .withInteractionField(PojoParameterParser.channelId(Parameters::setChannel))
                .withParameterField(
                        "after-time",
                        "Indicate that session must start after this time for you to attend. Default: whenever",
                        StringParameter.DEFAULT_EMPTY_STRING.map(DateUtil::parseCasualTime),
                        Parameters::setStartTime
                )
                .withParameterField(
                        "before-time",
                        "Indicate that session must end before this time for you to attend. Default: whenever",
                        StringParameter.DEFAULT_EMPTY_STRING.map(DateUtil::parseCasualTime),
                        Parameters::setEndTime
                )
                .withParameterField(
                        "timezone",
                        "Indicate the timezone that your time is. Can be a tzid, or ET/CT/MT/PT. Default: CT",
                        StringParameter.builder()
                                .defaultValue("CT").build()
                                .map(DateUtil::parseCasualTimezone),
                        Parameters::setTimezone
                )
        );
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        // i hate it i hate it i hate it
        var startTime = new ZonedDateTime[1];
        var endTime = new ZonedDateTime[1];
        var worked = nextSessionManager.playerDo(params.channel, params.user.getId(), (session, pr) -> {
           startTime[0] = params.startTime == null ? null : computeContextualTime(session.getProposedStartTime(), params.startTime, params.timezone);
           endTime[0] = params.endTime == null ? null : computeContextualTime(session.getProposedStartTime(), params.endTime, params.timezone);
           pr.yes(startTime[0], endTime[0]);
        });

        if (worked) {
            return CommandResponse.reply(getResponseString(params.user, startTime[0], endTime[0]), false);
        } else {
            return CommandResponse.reply("No upcoming session???", true);
        }
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

    private String getResponseString(User user, ZonedDateTime start, ZonedDateTime end) {
        var username = user.getUsername();
        if (start == null && end == null) {
            return String.format("%s will be able to attend session!", username);
        } else if (start == null) {
            return String.format("%s will be able to attend session, so long as it ends by %s!", username, FormatUtils.formatShortTime(end));
        } else if (end == null) {
            return String.format("%s will be able to attend session anytime after %s!", username, FormatUtils.formatShortTime(start));
        } else {
            return String.format("%s will be able to attend session anytime between %s and %s!", username,
                    FormatUtils.formatShortTime(start), FormatUtils.formatShortTime(end));
        }
    }

    @Data
    public static class Parameters {
        Snowflake channel;
        User user;
        LocalTime startTime;
        LocalTime endTime;
        ZoneId timezone;
    }
}
