package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.CommandPermission;
import com.github.milomarten.taisha_rangers2.command.localization.LocalizedCommandSpec;
import com.github.milomarten.taisha_rangers2.command.parameter.IntParameter;
import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("done")
public class DoneSessionCommand extends LocalizedCommandSpec<DoneSessionCommand.Parameters> {
    private final NextSessionManager manager;

    public DoneSessionCommand(NextSessionManager manager) {
        super("done");
        this.manager = manager;

        setParameterParser(SessionIdentityParameters.parser(Parameters::new)
                .withParameterField("exp", IntParameter.REQUIRED, Parameters::setExp)
                .withParameterField("other", StringParameter.DEFAULT_EMPTY_STRING, Parameters::setOtherStuff)
        );
        setPermissions(Set.of(CommandPermission.MANAGE_CHANNELS));
    }

    @Override
    public CommandResponse doAction(DoneSessionCommand.Parameters params) {
        var worked = manager.cancelSession(params);
        if (worked) {
            if (params.getOtherStuff().isEmpty()) {
                return localizationFactory.createResponse("command.done.response.exp-only", params.exp)
                        .ephemeral(false);
            } else {
                return localizationFactory.createResponse("command.done.response.exp-and-other", params.exp, params.otherStuff)
                        .ephemeral(false);
            }
        } else {
            return localizationFactory.createResponse("errors.session.no-match")
                    .ephemeral(true);
        }
    }

    @Getter
    @Setter
    public static class Parameters extends SessionIdentityParameters {
        private int exp;
        private String otherStuff;
    }
}
