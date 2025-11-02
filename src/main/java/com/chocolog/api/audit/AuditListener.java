package com.chocolog.api.audit;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.PreRemove;

public class AuditListener {

    @PostPersist
    public void onPostPersist(Object entity) {
        AuditHelper.saveAudit(entity, "CREATE");
    }

    @PreUpdate
    public void onPreUpdate(Object entity) {
        AuditHelper.saveAudit(entity, "UPDATE");
    }

    @PreRemove
    public void onPreRemove(Object entity) {
        AuditHelper.saveAudit(entity, "DELETE");
    }
}