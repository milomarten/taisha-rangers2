package com.github.milomarten.taisha_rangers2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.taisha_rangers2.persistence.JsonFilePersister;
import com.github.milomarten.taisha_rangers2.persistence.NoOpPersister;
import com.github.milomarten.taisha_rangers2.persistence.Persister;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {
    @Bean
    public Persister persister(@Value("${persistence.session-manager.base-path:}") String path,
                               ObjectMapper om) {
        return StringUtils.isEmpty(path) ?
                new NoOpPersister() :
                new JsonFilePersister(path, om);
    }
}
