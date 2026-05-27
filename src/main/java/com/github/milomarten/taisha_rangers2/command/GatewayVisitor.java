package com.github.milomarten.taisha_rangers2.command;

import discord4j.core.GatewayDiscordClient;

public interface GatewayVisitor {
    void visit(GatewayDiscordClient gateway);
}
