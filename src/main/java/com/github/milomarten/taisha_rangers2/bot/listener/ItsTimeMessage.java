package com.github.milomarten.taisha_rangers2.bot.listener;

import com.github.milomarten.taisha_rangers2.config.LocalizedDiscordService;
import com.github.milomarten.taisha_rangers2.state.NextSession;
import com.github.milomarten.taisha_rangers2.state.NextSessionListener;
import com.github.milomarten.taisha_rangers2.state.NextSessionManager;
import com.github.milomarten.taisha_rangers2.util.FormatUtils;
import discord4j.common.util.Snowflake;
import discord4j.core.spec.MessageCreateMono;
import discord4j.rest.util.AllowedMentions;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;

import java.util.function.UnaryOperator;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(prefix = "reminder", value = "enabled")
public class ItsTimeMessage extends BaseSessionScheduler<Snowflake> implements NextSessionListener {
    private final LocalizedDiscordService client;
    @Setter private NextSessionManager nextSessionManager;

    @Override
    public void onLoad(NextSession nextSession) {
        onUpdate(nextSession);
    }

    @Override
    public void onUpdate(NextSession session) {
        if (session.getStartTime() != null) {
            var newTime = timingHelper.getSessionStartReminderTime(session).toInstant();
            var oldTime = super.getScheduledTime(session.getChannel());
            if (oldTime == null || !oldTime.equals(newTime)) {
                schedule(
                        session.getChannel(),
                        () -> nextSessionManager.getNextSession(session.getChannel())
                                .ifPresent(this::pingToBegin),
                        newTime
                );
            }
        }
    }

    @Override
    public void onDelete(Snowflake channel) {
        cancel(channel);
    }

    private void pingToBegin(NextSession session) {
        client.sendLocalizedMessage(
                session.getChannel(),
                session.getLocale(),
                configureSafePings(session),
                "job.its-time",
                FormatUtils.pingUser(session.getGm()),
                session.getPing() == null ? "everyone" : FormatUtils.pingRole(session.getPing()),
                FormatUtils.formatRelativeTime(session.getStartTime())
        ).subscribe();
    }

    private UnaryOperator<MessageCreateMono> configureSafePings(NextSession session) {
        return msg -> {
            var allowed = AllowedMentions.builder().allowUser(session.getGm());
            if (session.getPing() != null) {
                allowed = allowed.allowRole(session.getPing());
            }
            return msg.withAllowedMentions(allowed.build());
        };
    }
}
