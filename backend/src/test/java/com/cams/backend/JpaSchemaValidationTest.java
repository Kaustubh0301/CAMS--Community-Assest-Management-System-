package com.cams.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import com.cams.backend.testsupport.CamsDataJpaTest;

/**
 * Boots the JPA layer with Hibernate schema <em>validation</em> (read-only:
 * never creates or alters objects). Startup fails if any entity disagrees with a
 * table, column, or column type in V1__init_schema.sql.
 */
@CamsDataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=validate")
class JpaSchemaValidationTest {

    private static final Set<String> V1_MVP_TABLES = Set.of(
            "local_body", "village_municipality", "ward",
            "user_account", "refresh_token",
            "asset_category", "asset", "asset_photo",
            "complaint", "complaint_photo", "complaint_status_history",
            "worker_assignment", "maintenance_history",
            "feedback", "notification", "audit_entry", "media_object");

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void entitiesValidateAgainstV1AndMapExactlyTheMvpTables() {
        assertThat(entityManagerFactory.getProperties())
                .containsEntry("hibernate.hbm2ddl.auto", "validate");

        Set<String> mappedTables = entityManagerFactory.getMetamodel().getEntities().stream()
                .map(entity -> entity.getJavaType().getAnnotation(Table.class).name())
                .collect(Collectors.toSet());

        assertThat(mappedTables).isEqualTo(V1_MVP_TABLES);
    }
}
