package com.github.milomarten.taisha_rangers2.config;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

@Configuration
public class DiscordConfig {
    @Bean
    @ConditionalOnProperty(value = "discord.token")
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public DiscordClient discordClient(@Value("${discord.token}") String token) {
        return DiscordClient.create(token);
    }

    @Bean
    @ConditionalOnBean(DiscordClient.class)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public GatewayDiscordClient gateway(DiscordClient client) {
        System.out.println("Starting Discord Bot...");
        return client.login().block();
    }
}
