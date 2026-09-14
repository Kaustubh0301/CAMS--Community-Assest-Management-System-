package com.cams.backend.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.auth.UserAccount;
import com.cams.backend.common.domain.EntityType;
import com.cams.backend.common.domain.Role;
import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class AuditEntryPersistenceTest {

    @Autowired
    private AuditEntryRepository auditEntries;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsActorActionEntityAndServerTimestamp() {
        UserAccount admin = PersistenceFixtures.user(entityManager, Role.ADMIN, null);
        UUID resetAccountId = PersistenceFixtures.user(entityManager, Role.WORKER,
                PersistenceFixtures.localBody(entityManager).getId()).getId();

        AuditEntry entry = auditEntries.save(new AuditEntry(admin.getId(), Role.ADMIN,
                AuditAction.PASSWORD_RESET, EntityType.USER_ACCOUNT, resetAccountId,
                "Reset at office counter", "req-123"));

        entityManager.flush();
        entityManager.clear();

        AuditEntry loaded = auditEntries.findById(entry.getId()).orElseThrow();
        assertThat(loaded.getActorId()).isEqualTo(admin.getId());
        assertThat(loaded.getActorRole()).isEqualTo(Role.ADMIN);
        assertThat(loaded.getAction()).isEqualTo(AuditAction.PASSWORD_RESET);
        assertThat(loaded.getEntityType()).isEqualTo(EntityType.USER_ACCOUNT);
        assertThat(loaded.getEntityId()).isEqualTo(resetAccountId);
        assertThat(loaded.getNote()).isEqualTo("Reset at office counter");
        assertThat(loaded.getRequestId()).isEqualTo("req-123");
        assertThat(loaded.getOccurredAt()).isNotNull();
    }

    @Test
    void persistsSystemActionWithoutActor() {
        AuditEntry entry = auditEntries.save(new AuditEntry(null, null, AuditAction.CATEGORY_CHANGE,
                EntityType.ASSET_CATEGORY, UUID.randomUUID(), null, null));

        entityManager.flush();
        entityManager.clear();

        AuditEntry loaded = auditEntries.findById(entry.getId()).orElseThrow();
        assertThat(loaded.getActorId()).isNull();
        assertThat(loaded.getActorRole()).isNull();
        assertThat(loaded.getAction()).isEqualTo(AuditAction.CATEGORY_CHANGE);
    }
}
