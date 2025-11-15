package com.chocolog.api.service;

import com.chocolog.api.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditPurgeService {

    private final AuditRepository auditRepository;

    @Value("${audit.retention.days:45}")
    private int retentionDays;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldAudits() {
        log.info("Starting audit records purge task.");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            long deletedRows = auditRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("Audit purge task finished. {} records older than {} were deleted.",
                    deletedRows, cutoffDate);

        } catch (Exception e) {
            log.error("Error during audit purge task: {}", e.getMessage(), e);
        }
    }
}