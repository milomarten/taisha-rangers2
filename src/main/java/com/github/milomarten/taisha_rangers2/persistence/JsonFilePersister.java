package com.github.milomarten.taisha_rangers2.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public class JsonFilePersister implements Persister {
    private final String basePath;
    private final ObjectMapper mapper;

    private File makeFile(String key) {
        return new File(basePath, key + ".json");
    }

    @Override
    public Mono<Void> persist(String key, Object t) {
        Objects.requireNonNull(t, "The object to persist is null");
        var file = makeFile(key);
        Mono<Void> mono = Mono.using(
                () -> new FileOutputStream(file),
                fis -> {
                    try {
                        fis.write(mapper.writeValueAsBytes(t));
                        return Mono.empty();
                    } catch (IOException e) {
                        log.error("Error while persisting {}", key, e);
                        return Mono.error(e);
                    }
                }
        ).then();
        if (!file.exists()) {
            mono = Mono.fromCallable(file::createNewFile)
                    .doOnSuccess(b -> {
                        if (b) {
                            log.info("Created file {}", file.getPath());
                        }
                    })
                    .then(mono);
        }
        return mono;
    }

    @Override
    public <T> Mono<T> load(String key, Class<T> clazz) {
        var file = makeFile(key);
        if (file.exists()) {
            return Mono.using(
                    () -> new FileInputStream(file),
                    fis -> {
                        try {
                            return Mono.just(mapper.readValue(fis, clazz));
                        } catch (IOException e) {
                            log.error("Error while loading {}", file.getPath(), e);
                            return Mono.error(e);
                        }
                    }
            );
        } else {
            log.info("File {} does not exist. Nothing was loaded.", file.getPath());
            return Mono.empty();
        }
    }

    @Override
    public <T> Mono<T> load(String key, TypeReference<T> clazz) {
        var file = makeFile(key);
        if (file.exists()) {
            return Mono.using(
                    () -> new FileInputStream(file),
                    fis -> {
                        try {
                            return Mono.just(mapper.readValue(fis, clazz));
                        } catch (IOException e) {
                            log.error("Error while loading {}", file.getPath(), e);
                            return Mono.error(e);
                        }
                    }
            );
        } else {
            log.info("File {} does not exist. Nothing was loaded.", file.getPath());
            return Mono.empty();
        }
    }
}
