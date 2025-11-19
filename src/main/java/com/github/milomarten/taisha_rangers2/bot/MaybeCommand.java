package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;

@Component("maybe")
public class MaybeCommand extends CommandSpec<MaybeCommand.Parameters> {
    private static final Duration MINUMUM = Duration.ofMinutes(15);

    private final NextSessionManager nextSessionManager;
    private final TimingHelper timingHelper;

    public MaybeCommand(NextSessionManager nextSessionManager, TimingHelper timingHelper) {
        super("maybe", "Indicate that you need to wait a bit before knowing if you can join session");
        this.nextSessionManager = nextSessionManager;
        this.timingHelper = timingHelper;

        setParameterParser(new PojoParameterParser<>(Parameters::new)
                .withParameterField(PojoParameterParser.channelId(), Parameters::setChannelId)
                .withParameterField(ChatInputInteractionEvent::getUser, Parameters::setUser)
                .withParameterField(
                        "duration",
                        "I'll send a message to you in this duration to remind you",
                        StringParameter.REQUIRED,
                        (p, s) -> p.setTimeFromNow(DateUtil.parseCasualDuration(s))
                )
        );
    }

    @Override
    public CommandResponse doAction(Parameters params) {
        if (MINUMUM.compareTo(params.timeFromNow) > 0) {
            return CommandResponse.reply("Duration is too small, must be more than 15 minutes.", true);
        }

        return nextSessionManager.playerDoAndReturn(
                params.channelId,
                params.user.getId(),
                (session, pr) -> {
                    var reminderTime = ZonedDateTime.now().plus(params.timeFromNow).withSecond(0);
                    var latestSubmitTime = timingHelper.getLatestStatusSubmitTime(session);
                    if (reminderTime.isBefore(latestSubmitTime)) {
                        pr.maybe(reminderTime);
                        var text = String.format("%s may be able to come. I'll check back with them at %s",
                                params.user.getUsername(), FormatUtils.formatShortDateTime(reminderTime));
                        return CommandResponse.reply(text, false);
                    } else {
                        var text = "That reminder would come too late. We need your answer by %s!";
                        return CommandResponse.reply(
                                String.format(text, FormatUtils.formatShortDateTime(latestSubmitTime)), true);
                    }
                }
        ).orElseGet(() -> CommandResponse.reply("No session???", true));
    }

    @Data
    public static class Parameters {
        private Snowflake channelId;
        private User user;
        private Duration timeFromNow;
    }
}
