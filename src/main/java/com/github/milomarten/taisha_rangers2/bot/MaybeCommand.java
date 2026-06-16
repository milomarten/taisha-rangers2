package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.DiscordEventListener;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Strings;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;

@Component("maybe")
public class MaybeCommand extends AbstractSessionPlayerCommand<MaybeCommand.Parameters> {
    private static final Duration MINUMUM = Duration.ofMinutes(15);

    private final TimingHelper timingHelper;

    public MaybeCommand(TimingHelper timingHelper) {
        super("maybe");
        this.timingHelper = timingHelper;

        setParameterParser(SessionIdentityParameters.parser(Parameters::new)
                .withParameterField(
                        "duration",
                        StringParameter.REQUIRED,
                        (p, s) -> p.setTimeFromNow(DateUtil.parseCasualDuration(s))
                )
        );
    }


    @Override
    protected CommandResponse doAction(Parameters params) {
        if (MINUMUM.compareTo(params.timeFromNow) > 0) {
            return localizationFactory.createResponse("command.maybe.error.duration-too-small")
                    .ephemeral(true);
        }
        return super.doAction(params);
    }

    @Override
    protected CommandResponse doPlayerAction(Parameters params, NextSession session, PlayerResponse pr) {
        var reminderTime = ZonedDateTime.now().plus(params.timeFromNow).withSecond(0);
        var latestSubmitTime = timingHelper.getLatestStatusSubmitTime(session);
        if (reminderTime.isBefore(latestSubmitTime)) {
            pr.maybe(reminderTime);
            return localizationFactory.createResponse("command.maybe.response",
                    params.getUsername(), FormatUtils.formatShortDateTime(reminderTime));
        } else {
            return localizationFactory.createResponse("command.maybe.error.duration-too-large", FormatUtils.formatShortDateTime(latestSubmitTime))
                    .ephemeral(true);
        }
    }

    @DiscordEventListener
    public Mono<?> handleMaybeButton(ButtonInteractionEvent button) {
        if (Strings.CS.startsWith(button.getCustomId(), "maybe")) {
            var rawDuration = button.getCustomId().split("-")[1];

            var parameters = new Parameters();
            parameters.setUser(button.getUser());
            parameters.setChannelId(button.getInteraction().getChannelId());
            parameters.setTimeFromNow(Duration.parse(rawDuration));

            return doAction(parameters).respond(button);
        }
        return Mono.empty();
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionIdentityParameters {
        private Duration timeFromNow;
    }
}
