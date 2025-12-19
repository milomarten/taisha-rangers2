package com.github.milomarten.taisha_rangers2.config;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.MessageCreateMono;
import discord4j.rest.util.AllowedMentions;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(GatewayDiscordClient.class)
public class LocalizedDiscordService {
    private final MessageSource messageSource;
    private final GatewayDiscordClient gateway;

    public Mono<Void> sendLocalizedMessage(Snowflake channelId, Locale locale, String key, Object... params) {
        return sendLocalizedMessage(channelId, locale, UnaryOperator.identity(), key, params);
    }

    public Mono<Void> sendLocalizedMessage(Snowflake channelId, Locale locale, UnaryOperator<MessageCreateMono> customizer, String key, Object... params) {
        return gateway.getChannelById(channelId)
                .cast(TextChannel.class)
                .flatMap(tc -> {
                    var message = messageSource.getMessage(key, params, locale);
                    return customizer.apply(tc.createMessage(message));
                })
                .then();
    }

    public static UnaryOperator<MessageCreateMono> withAllowedRoleMention(Snowflake ping) {
        return msg -> {
            if (ping != null) {
                return msg.withAllowedMentions(AllowedMentions.builder().allowRole(ping).build());
            } else {
                return msg;
            }
        };
    }

    public static UnaryOperator<MessageCreateMono> withAllowedUserMention(Snowflake ping) {
        return msg -> {
            if (ping != null) {
                return msg.withAllowedMentions(AllowedMentions.builder().allowUser(ping).build());
            } else {
                return msg;
            }
        };
    }

    public static UnaryOperator<MessageCreateMono> withAllowedUserMention(List<Snowflake> pings) {
        return msg -> {
            if (pings != null && !pings.isEmpty()) {
                var ams = pings.toArray(Snowflake[]::new);
                return msg.withAllowedMentions(AllowedMentions.builder().allowUser(ams).build());
            } else {
                return msg;
            }
        };
    }
}
