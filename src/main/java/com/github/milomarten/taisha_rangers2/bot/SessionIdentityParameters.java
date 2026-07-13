package com.github.milomarten.taisha_rangers2.bot;

import com.github.milomarten.taisha_rangers2.command.parameter.ParameterInfo;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.entity.User;
import lombok.*;

import java.util.function.Supplier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionIdentityParameters {
    private User user;
    private Snowflake channelId;

    public Snowflake getUserId() {
        return user.getId();
    }

    public String getUsername() {
        return user.getGlobalName().orElseGet(user::getUsername);
    }

    public static PojoParameterParser<SessionIdentityParameters> parser() {
        return new PojoParameterParser<>(SessionIdentityParameters::new)
                .withParameterField(InteractionCreateEvent::getUser, SessionIdentityParameters::setUser)
                .withParameterField(PojoParameterParser.channelId(), SessionIdentityParameters::setChannelId);
    }

    public static <T extends SessionIdentityParameters> PojoParameterParser<T> parser(Supplier<T> constructor) {
        return new PojoParameterParser<>(constructor)
                .withParameterField(InteractionCreateEvent::getUser, SessionIdentityParameters::setUser)
                .withParameterField(PojoParameterParser.channelId(), SessionIdentityParameters::setChannelId);
    }

    @Getter
    @Setter
    public static class One<T> extends SessionIdentityParameters {
        private T item;

        public static <T> PojoParameterParser<One<T>> parser(String id, ParameterInfo<T> parameterInfo) {
            return SessionIdentityParameters.<One<T>>parser(One::new)
                    .withParameterField(id, parameterInfo, One::setItem);
        }
    }
}
