package com.github.milomarten.taisha_rangers2.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

public class NoOpPersister implements Persister {
    @Override
    public Mono<Void> persist(String key, Object t) {
        return Mono.empty();
    }

    @Override
    public <T> Mono<T> load(String key, Class<T> clazz) {
        return Mono.empty();
    }

    @Override
    public <T> Mono<T> load(String key, TypeReference<T> clazz) {
        return Mono.empty();
    }
}
