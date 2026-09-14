package com.cams.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.asset.AssetCondition;
import com.cams.backend.asset.AssetStatus;
import com.cams.backend.audit.AuditAction;
import com.cams.backend.auth.AccountStatus;
import com.cams.backend.auth.Language;
import com.cams.backend.common.domain.EntityType;
import com.cams.backend.common.domain.Role;
import com.cams.backend.complaint.ComplaintEvent;
import com.cams.backend.complaint.ComplaintPhotoKind;
import com.cams.backend.complaint.ComplaintPriority;
import com.cams.backend.complaint.ComplaintStatus;
import com.cams.backend.localbody.LocalBodyType;
import com.cams.backend.media.MediaPurpose;
import com.cams.backend.notification.NotificationType;
import com.cams.backend.testsupport.CamsDataJpaTest;

/**
 * Every Java enum must have exactly the labels, in the same order, of its
 * PostgreSQL enum type in V1, and no V1 enum type may be left unmapped.
 */
@CamsDataJpaTest
class EnumMappingConsistencyTest {

    private static final Map<String, Class<? extends Enum<?>>> V1_ENUM_TYPES = new LinkedHashMap<>();

    static {
        V1_ENUM_TYPES.put("role", Role.class);
        V1_ENUM_TYPES.put("account_status", AccountStatus.class);
        V1_ENUM_TYPES.put("language", Language.class);
        V1_ENUM_TYPES.put("local_body_type", LocalBodyType.class);
        V1_ENUM_TYPES.put("asset_status", AssetStatus.class);
        V1_ENUM_TYPES.put("asset_condition", AssetCondition.class);
        V1_ENUM_TYPES.put("complaint_status", ComplaintStatus.class);
        V1_ENUM_TYPES.put("complaint_priority", ComplaintPriority.class);
        V1_ENUM_TYPES.put("complaint_event", ComplaintEvent.class);
        V1_ENUM_TYPES.put("complaint_photo_kind", ComplaintPhotoKind.class);
        V1_ENUM_TYPES.put("notification_type", NotificationType.class);
        V1_ENUM_TYPES.put("audit_action", AuditAction.class);
        V1_ENUM_TYPES.put("entity_type", EntityType.class);
        V1_ENUM_TYPES.put("media_purpose", MediaPurpose.class);
    }

    @Autowired
    private EntityManager entityManager;

    @Test
    void everyV1EnumTypeIsMapped() {
        @SuppressWarnings("unchecked")
        List<String> databaseEnumTypes = entityManager.createNativeQuery("""
                select t.typname from pg_type t
                join pg_namespace n on n.oid = t.typnamespace
                where n.nspname = 'public' and t.typtype = 'e'
                """).getResultList();

        assertThat(databaseEnumTypes).containsExactlyInAnyOrderElementsOf(V1_ENUM_TYPES.keySet());
    }

    @Test
    void javaEnumLabelsMatchDatabaseLabelsInOrder() {
        V1_ENUM_TYPES.forEach((typeName, enumClass) -> {
            Object databaseLabels = entityManager.createNativeQuery("""
                    select string_agg(e.enumlabel, ',' order by e.enumsortorder)
                    from pg_enum e
                    join pg_type t on t.oid = e.enumtypid
                    join pg_namespace n on n.oid = t.typnamespace
                    where n.nspname = 'public' and t.typname = :typeName
                    """).setParameter("typeName", typeName).getSingleResult();

            String javaLabels = Arrays.stream(enumClass.getEnumConstants())
                    .map(Enum::name)
                    .collect(Collectors.joining(","));

            assertThat(javaLabels).as("enum type %s", typeName).isEqualTo(databaseLabels);
        });
    }

    @Test
    void complaintStatusHasNoReturnedState() {
        assertThat(ComplaintStatus.values()).extracting(Enum::name).doesNotContain("RETURNED");
    }
}
