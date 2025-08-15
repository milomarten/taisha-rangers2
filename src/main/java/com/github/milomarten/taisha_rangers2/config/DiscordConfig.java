package com.github.milomarten.taisha_rangers2.config;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfig {
    @Bean
    @ConditionalOnProperty(value = "discord.token")
    public DiscordClient discordClient(@Value("${discord.token}") String token) {
        return DiscordClient.create(token);
    }

    @Bean
    @ConditionalOnBean(DiscordClient.class)
    public GatewayDiscordClient gateway(DiscordClient client) {
        System.out.println("Starting Discord Bot...");
        return client.login().block();
    }
}
