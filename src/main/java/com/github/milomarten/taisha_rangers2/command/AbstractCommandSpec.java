package com.github.milomarten.taisha_rangers2.command;

import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Setter;

import java.math.BigInteger;
import java.util.Optional;
import java.util.Set;

@Setter
public abstract class AbstractCommandSpec implements CommandHandler {
    Set<CommandPermission> permissions = Set.of();

    @Override
    public ApplicationCommandRequest toDiscordSpec() {
        return decorate(ApplicationCommandRequest.builder()
                .defaultMemberPermissions(computeMemberPermissions())
        ).build();
    }

    protected abstract ImmutableApplicationCommandRequest.Builder decorate(ImmutableApplicationCommandRequest.Builder builder);

    private Optional<String> computeMemberPermissions() {
        if (permissions.isEmpty()) {
            return Optional.empty();
        }
        BigInteger perms = BigInteger.ZERO;
        for (var perm : permissions) {
            BigInteger permAsBigInt = BigInteger.ONE.shiftLeft(perm.bitPosition);
            perms = perms.or(permAsBigInt);
        }
        return Optional.of(perms.toString());
    }
}
