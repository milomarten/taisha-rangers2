package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.bot.TimingHelper;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ConditionalOnBooleanProperty(prefix = "reminder", value = "enabled")
public class DoneReminderMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final GatewayDiscordClient client;
    @Setter private NextSessionManager nextSessionManager;
    private final TimingHelper timingHelper;

    @Override
    public void onLoad(NextSession nextSession) {
        onUpdate(nextSession);
    }

    @Override
    public void onUpdate(NextSession session) {
        if (session.getStartTime() != null) {
            schedule(
                    session.getChannel(),
                    () -> nextSessionManager.getNextSession(session.getChannel())
                            .ifPresent(this::sendReminderMessage),
                    timingHelper.getDoneReminderTime(session).toInstant()
            );
        }
    }

    @Override
    public void onDelete(Snowflake channel) {
        cancel(channel);
    }

    private void sendReminderMessage(NextSession nextSession) {
        client.getChannelById(nextSession.getChannel())
                .cast(TextChannel.class)
                .flatMap(tc -> {
                    return tc.createMessage(String.format(
                            "Hey %s! Doofus! You forgot to use `/done`!!",
                            FormatUtils.pingUser(nextSession.getGm()))
                    );
                }).subscribe();
    }
}
