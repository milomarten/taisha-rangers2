package com.github.milomarten.taisha_rangers2.command;

import com.github.milomarten.taisha_rangers2.bot.InitializeSessionCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class CommandSpecTest {
    @Autowired private InitializeSessionCommand initializeSessionCommand;

    @Test
    public void testInitSessionSpec() {
        var spec = initializeSessionCommand.toDiscordSpec();
        System.out.println(spec);
    }
}