package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import com.github.milomarten.taisha_rangers2.util.DateUtil;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

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

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends SessionIdentityParameters {
        private Duration timeFromNow;
    }
}
