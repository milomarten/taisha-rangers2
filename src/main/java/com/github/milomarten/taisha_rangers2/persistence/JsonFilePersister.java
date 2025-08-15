package com.github.milomarten.taisha_rangers2.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
public class JsonFilePersister implements Persister {
    private final String basePath;
    private final ObjectMapper mapper;

    private File makeFile(String key) {
        return new File(basePath, key + ".json");
    }

    @Override
    public Mono<Void> persist(String key, Object t) {
        if (t == null) {
            return Mono.empty();
        }
        return Mono.using(
                () -> new FileOutputStream(makeFile(key)),
                fis -> {
                    try {
                        fis.write(mapper.writeValueAsBytes(t));
                        return Mono.empty();
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                }
        ).then();
    }

    @Override
    public <T> Mono<T> load(String key, Class<T> clazz) {
        var file = makeFile(key);
        return Mono.using(
                () -> new FileInputStream(file),
                fis -> {
                    try {
                        if (file.exists()) {
                            return Mono.just(mapper.readValue(fis, clazz));
                        } else {
                            return Mono.empty();
                        }
                    } catch (IOException e) {
                        return Mono.error(e);
                    }
                }
        );
    }
}
