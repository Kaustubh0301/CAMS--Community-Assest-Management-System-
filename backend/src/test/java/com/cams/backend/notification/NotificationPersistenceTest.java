package com.cams.backend.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.common.domain.EntityType;
import com.cams.backend.complaint.Complaint;
import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class NotificationPersistenceTest {

    @Autowired
    private NotificationRepository notifications;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsJsonbBodyParamsAndRelatedEntity() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);

        Notification notification = new Notification();
        notification.setRecipientId(complaint.getReportedBy());
        notification.setType(NotificationType.COMPLAINT_CLOSED);
        notification.setTitleKey("notification.complaint.closed");
        notification.setBodyParams("{\"ward\": \"7A\", \"assetCode\": \"AST-1\"}");
        notification.setRelatedEntityType(EntityType.COMPLAINT);
        notification.setRelatedEntityId(complaint.getId());
        notifications.save(notification);

        entityManager.flush();
        entityManager.clear();

        Notification loaded = notifications.findById(notification.getId()).orElseThrow();
        assertThat(loaded.getType()).isEqualTo(NotificationType.COMPLAINT_CLOSED);
        assertThat(loaded.getRelatedEntityType()).isEqualTo(EntityType.COMPLAINT);
        assertThat(loaded.getRelatedEntityId()).isEqualTo(complaint.getId());
        assertThat(loaded.getBodyParams()).contains("\"ward\"").contains("\"7A\"");
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getReadAt()).isNull();

        Object[] stored = (Object[]) entityManager.createNativeQuery("""
                select jsonb_typeof(body_params), body_params ->> 'assetCode'
                from notification where id = :id
                """).setParameter("id", notification.getId()).getSingleResult();
        assertThat(stored).containsExactly("object", "AST-1");
    }

    @Test
    void persistsNotificationWithoutOptionalFieldsAndMarksRead() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);

        Notification notification = new Notification();
        notification.setRecipientId(complaint.getReportedBy());
        notification.setType(NotificationType.COMPLAINT_SUBMITTED);
        notification.setTitleKey("notification.complaint.submitted");
        notifications.save(notification);
        entityManager.flush();

        notification.setReadAt(Instant.now());
        entityManager.flush();
        entityManager.clear();

        Notification loaded = notifications.findById(notification.getId()).orElseThrow();
        assertThat(loaded.getBodyParams()).isNull();
        assertThat(loaded.getRelatedEntityType()).isNull();
        assertThat(loaded.getReadAt()).isNotNull();
    }
}
