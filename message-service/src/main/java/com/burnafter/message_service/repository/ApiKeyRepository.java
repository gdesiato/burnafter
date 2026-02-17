package com.burnafter.message_service.repository;

import com.burnafter.message_service.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByPrefixAndEnabledTrue(String prefix);

}
