package com.chocolog.api.audit;

import com.chocolog.api.model.Audit;
import com.chocolog.api.model.Employee;
import com.chocolog.api.repository.AuditRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class AuditHelper {
    private static AuditRepository staticAuditRepository;
    private static AuditorAware<Employee> staticAuditorAware;
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final AuditRepository auditRepository;
    private final AuditorAware<Employee> auditorAware;

    @PostConstruct
    public void init() {
        AuditHelper.staticAuditRepository = this.auditRepository;
        AuditHelper.staticAuditorAware = this.auditorAware;
    }

    public static void saveAudit(Object entity, String action) {
        staticAuditorAware.getCurrentAuditor().ifPresent(employee -> {
            if (!(entity instanceof Auditable)) {
                return;
            }
            Auditable auditableEntity = (Auditable) entity;

            String changedData = "";
            if (!action.equals("DELETE")) {
                try {
                    changedData = objectMapper.writeValueAsString(entity);
                } catch (Exception e) {
                    changedData = "Error to serialize data.";
                }
            }

            Audit audit = Audit.builder()
                    .employee(employee)
                    .entityName(entity.getClass().getSimpleName())
                    .entityId(auditableEntity.getId())
                    .action(action)
                    .changedData(changedData)
                    .createdAt(LocalDateTime.now())
                    .build();

            staticAuditRepository.save(audit);
        });
    }
}