package com.github.milomarten.taisha_rangers2.repository;

import com.github.milomarten.taisha_rangers2.repository.model.PartyDB;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartyRepository extends CrudRepository<PartyDB, Long> {
    Optional<PartyDB> findByName(String name);
}
