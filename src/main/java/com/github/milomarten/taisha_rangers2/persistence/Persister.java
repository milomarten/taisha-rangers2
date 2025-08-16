package com.github.milomarten.taisha_rangers2.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

public interface Persister {
    Mono<Void> persist(String key, Object t);

    <T> Mono<T> load(String key, Class<T> clazz);

    <T> Mono<T> load(String key, TypeReference<T> clazz);
}
