package com.cams.backend.media;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.auth.UserAccount;
import com.cams.backend.common.domain.Role;
import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class MediaObjectPersistenceTest {

    @Autowired
    private MediaObjectRepository mediaObjects;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsMetadataOnly() {
        UserAccount worker = PersistenceFixtures.user(entityManager, Role.WORKER,
                PersistenceFixtures.localBody(entityManager).getId());

        MediaObject media = new MediaObject();
        media.setStorageKey(PersistenceFixtures.unique("complaint/2026/09") + ".jpg");
        media.setContentType("image/jpeg");
        media.setSizeBytes(3_500_000L);
        media.setWidth(1600);
        media.setHeight(1200);
        media.setChecksum("a".repeat(64));
        media.setPurpose(MediaPurpose.COMPLAINT_PHOTO);
        media.setUploadedBy(worker.getId());
        mediaObjects.save(media);

        entityManager.flush();
        entityManager.clear();

        MediaObject loaded = mediaObjects.findById(media.getId()).orElseThrow();
        assertThat(loaded.getSizeBytes()).isEqualTo(3_500_000L);
        assertThat(loaded.getWidth()).isEqualTo(1600);
        assertThat(loaded.getHeight()).isEqualTo(1200);
        assertThat(loaded.getPurpose()).isEqualTo(MediaPurpose.COMPLAINT_PHOTO);
        assertThat(loaded.getUploadedBy()).isEqualTo(worker.getId());
        assertThat(loaded.getUploadedAt()).isNotNull();
    }
}
