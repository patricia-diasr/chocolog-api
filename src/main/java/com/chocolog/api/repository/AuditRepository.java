package com.chocolog.api.repository;

import com.chocolog.api.model.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;

public interface AuditRepository extends JpaRepository<Audit, Long> {

    @Modifying
    long deleteByCreatedAtBefore(LocalDateTime cutoffDate);

}
