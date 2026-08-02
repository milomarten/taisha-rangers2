package com.github.milomarten.taisha_rangers2.bot.scar;

import com.github.milomarten.taisha_rangers2.bot.SessionIdentityParameters;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizationFactory;
import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.ParameterParser;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.exception.NotDM;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScarDoneCommand {
    private final NextSessionManager nextSessionManager;
    private final LocalizationFactory localizationFactory;

    public static ParameterParser<Parameters> parser() {
        return ScarCommand.ScarIdentityParameters.parser(Parameters::new)
                .withParameterField("exp", IntParameter.builder().minValue(0).build(), Parameters::setExp)
                .withParameterField("merits", IntParameter.builder().minValue(0).build(), Parameters::setMerits)
                .withParameterField("other", StringParameter.DEFAULT_EMPTY_STRING, Parameters::setOther);
    }

    public CommandResponse run(Parameters parameters) {
        try {
            var worked = nextSessionManager.cancelSession(new SessionIdentityParameters(parameters.getUser(), parameters.getChannelId()));
            if (worked) {
                if (StringUtils.isEmpty(parameters.other)) {
                    return localizationFactory.createResponse("command.scar.subcommand.done.response.no-stuff", parameters.exp, parameters.merits)
                            .ephemeral(false);
                } else {
                    return localizationFactory.createResponse("command.scar.subcommand.done.response.with-stuff", parameters.exp, parameters.merits, parameters.other)
                            .ephemeral(false);
                }
            } else {
                return localizationFactory.createResponse("errors.session.no-match")
                        .ephemeral(true);
            }
        } catch (NotDM ex) {
            return localizationFactory.createResponse("errors.session.no-access")
                    .ephemeral(true);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Parameters extends ScarCommand.ScarIdentityParameters {
        private int exp;
        private int merits;
        private String other;
    }
}
