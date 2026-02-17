package com.burnafter.message_service.repository;

import com.burnafter.message_service.model.Paste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface PasteRepository extends JpaRepository<Paste, UUID> {
    List<Paste> findByExpireAtBefore(Instant now);
}
