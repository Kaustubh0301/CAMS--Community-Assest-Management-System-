package com.cams.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.testsupport.CamsDataJpaTest;

/**
 * Guards the approved configuration: Flyway owns the schema, and Hibernate must
 * never create or update it (application.yml: spring.jpa.hibernate.ddl-auto=none).
 * With "none", Spring Boot omits the Hibernate setting entirely, so an absent key
 * and an explicit "none" are both accepted.
 */
@CamsDataJpaTest
class JpaSchemaGenerationDisabledTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void hibernateSchemaGenerationIsDisabled() {
        Map<String, Object> properties = entityManagerFactory.getProperties();

        assertThat(String.valueOf(properties.getOrDefault("hibernate.hbm2ddl.auto", "none")))
                .isEqualToIgnoringCase("none");
        assertThat(String.valueOf(properties.getOrDefault(
                "jakarta.persistence.schema-generation.database.action", "none")))
                .isEqualToIgnoringCase("none");
    }
}
