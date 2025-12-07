package com.github.milomarten.taisha_rangers2.bot.party;

import com.github.milomarten.taisha_rangers2.command.parameter.StringParameter;
import com.github.milomarten.taisha_rangers2.command.parameters.PojoParameterParser;
import discord4j.common.util.Snowflake;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyIdentityParameters {
    private String partyName;
    private Snowflake userId;

    public static PojoParameterParser<PartyIdentityParameters> parser() {
        return new PojoParameterParser<>(PartyIdentityParameters::new)
                .withParameterField("party-name", StringParameter.REQUIRED, PartyIdentityParameters::setPartyName)
                .withParameterField(PojoParameterParser.userId(), PartyIdentityParameters::setUserId);
    }

    public static <T extends PartyIdentityParameters> PojoParameterParser<T> parser(Supplier<T> constructor) {
        return new PojoParameterParser<>(constructor)
                .withParameterField("party-name", StringParameter.REQUIRED, PartyIdentityParameters::setPartyName)
                .withParameterField(PojoParameterParser.userId(), PartyIdentityParameters::setUserId);
    }
}
