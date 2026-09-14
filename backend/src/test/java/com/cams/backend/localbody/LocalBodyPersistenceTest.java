package com.cams.backend.localbody;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.testsupport.CamsDataJpaTest;

@CamsDataJpaTest
class LocalBodyPersistenceTest {

    @Autowired
    private LocalBodyRepository localBodies;

    @Autowired
    private VillageMunicipalityRepository villages;

    @Autowired
    private WardRepository wards;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsHierarchyWithNativeEnumDefaultsAndTimestamps() {
        LocalBody body = new LocalBody();
        body.setName("Test Municipality");
        body.setCode("TEST-" + UUID.randomUUID());
        body.setType(LocalBodyType.MUNICIPALITY);
        localBodies.save(body);

        VillageMunicipality village = new VillageMunicipality();
        village.setLocalBody(body);
        village.setName("Test Township");
        villages.save(village);

        Ward ward = new Ward();
        ward.setVillageMunicipality(village);
        ward.setName("Ward 7A");
        ward.setWardNumber("7A");
        ward.setCentroidLat(new BigDecimal("23.022505"));
        ward.setCentroidLng(new BigDecimal("72.571365"));
        wards.save(ward);

        entityManager.flush();
        entityManager.clear();

        Ward loaded = wards.findById(ward.getId()).orElseThrow();
        assertThat(loaded.getWardNumber()).isEqualTo("7A");
        assertThat(loaded.getCentroidLat()).isEqualByComparingTo("23.022505");
        assertThat(loaded.getCentroidLng()).isEqualByComparingTo("72.571365");
        assertThat(loaded.isActive()).isTrue();
        assertThat(loaded.getCreatedAt()).isNotNull();

        VillageMunicipality loadedVillage = loaded.getVillageMunicipality();
        assertThat(loadedVillage.getId()).isEqualTo(village.getId());
        assertThat(loadedVillage.getCode()).isNull();

        LocalBody loadedBody = loadedVillage.getLocalBody();
        assertThat(loadedBody.getType()).isEqualTo(LocalBodyType.MUNICIPALITY);
        assertThat(loadedBody.isActive()).isTrue();
        assertThat(loadedBody.getCreatedAt()).isNotNull();
        assertThat(loadedBody.getCreatedBy()).isNull();

        Object storedType = entityManager
                .createNativeQuery("select type::text from local_body where id = :id")
                .setParameter("id", body.getId())
                .getSingleResult();
        assertThat(storedType).isEqualTo("MUNICIPALITY");
    }
}
