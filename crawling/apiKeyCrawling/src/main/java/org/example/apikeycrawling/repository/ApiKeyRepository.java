package org.example.apikeycrawling.repository;

import org.example.apikeycrawling.entity.mysql.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

}
