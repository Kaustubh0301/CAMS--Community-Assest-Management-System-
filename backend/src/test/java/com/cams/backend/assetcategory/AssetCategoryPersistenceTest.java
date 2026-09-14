package com.cams.backend.assetcategory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class AssetCategoryPersistenceTest {

    @Autowired
    private AssetCategoryRepository categories;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsCategoryScopedToLocalBodyWithDefaults() {
        UUID localBodyId = PersistenceFixtures.localBody(entityManager).getId();

        AssetCategory custom = new AssetCategory();
        custom.setLocalBodyId(localBodyId);
        custom.setName("Hand Pump");
        categories.save(custom);

        AssetCategory seeded = new AssetCategory();
        seeded.setLocalBodyId(localBodyId);
        seeded.setName("Street Light");
        seeded.setSeed(true);
        categories.save(seeded);

        entityManager.flush();
        entityManager.clear();

        AssetCategory loadedCustom = categories.findById(custom.getId()).orElseThrow();
        assertThat(loadedCustom.getLocalBodyId()).isEqualTo(localBodyId);
        assertThat(loadedCustom.isActive()).isTrue();
        assertThat(loadedCustom.isSeed()).isFalse();
        assertThat(loadedCustom.getCreatedAt()).isNotNull();

        assertThat(categories.findById(seeded.getId()).orElseThrow().isSeed()).isTrue();
    }
}
