package org.example.garencrawling.mostchampion.repository;

import org.example.garencrawling.mostchampion.domain.writemongo.PlayerMost;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerMostRepository extends MongoRepository<PlayerMost, Integer> {
}