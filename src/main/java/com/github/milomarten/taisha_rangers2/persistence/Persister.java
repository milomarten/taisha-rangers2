package com.github.milomarten.taisha_rangers2.persistence;

import reactor.core.publisher.Mono;

public interface Persister {
    Mono<Void> persist(String key, Object t);

    <T> Mono<T> load(String key, Class<T> clazz);
}
