package com.burnafter.burnafter.repository;

import com.burnafter.burnafter.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByPrefixAndEnabledTrue(String prefix);

}
