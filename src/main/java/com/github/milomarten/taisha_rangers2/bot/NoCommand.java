package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.DiscordEventListener;
import com.github.milomarten.taisha_rangers2.command.response.CommandResponse;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.PlayerResponse;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("no")
public class NoCommand extends AbstractSessionPlayerCommand<SessionIdentityParameters> {
    public NoCommand() {
        super("no");

        this.setParameterParser(SessionIdentityParameters.parser());
    }

    @Override
    protected CommandResponse doPlayerAction(SessionIdentityParameters params, NextSession session, PlayerResponse pr) {
        pr.no();
        return localizationFactory.createResponse("command.no.response", params.getUsername());
    }

    @DiscordEventListener
    public Mono<?> handleNoButton(ButtonInteractionEvent button) {
        if ("no".equalsIgnoreCase(button.getCustomId())) {
            var p = new SessionIdentityParameters();
            p.setUser(button.getUser());
            p.setChannelId(button.getInteraction().getChannelId());

            return doAction(p).respond(button);
        }
        return Mono.empty();
    }
}
