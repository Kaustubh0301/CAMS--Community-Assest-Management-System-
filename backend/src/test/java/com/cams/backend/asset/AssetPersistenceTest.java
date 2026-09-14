package com.cams.backend.asset;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.assetcategory.AssetCategory;
import com.cams.backend.auth.UserAccount;
import com.cams.backend.common.domain.Role;
import com.cams.backend.localbody.LocalBody;
import com.cams.backend.localbody.Ward;
import com.cams.backend.media.MediaObject;
import com.cams.backend.media.MediaPurpose;
import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class AssetPersistenceTest {

    @Autowired
    private AssetRepository assets;

    @Autowired
    private AssetPhotoRepository assetPhotos;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAssetWithIndependentStatusAndConditionCoordinatesAndDates() {
        LocalBody body = PersistenceFixtures.localBody(entityManager);
        Ward ward = PersistenceFixtures.ward(entityManager, body);
        AssetCategory category = PersistenceFixtures.assetCategory(entityManager, body.getId());
        UserAccount officer = PersistenceFixtures.user(entityManager, Role.OFFICER, body.getId());

        Asset asset = new Asset();
        asset.setWardId(ward.getId());
        asset.setLocalBodyId(body.getId());
        asset.setAssetCategoryId(category.getId());
        asset.setPublicCode(PersistenceFixtures.unique("AST"));
        asset.setStatus(AssetStatus.BROKEN);
        asset.setCondition(AssetCondition.FAIR);
        asset.setLatitude(new BigDecimal("-12.345678"));
        asset.setLongitude(new BigDecimal("123.456789"));
        asset.setInstallationDate(LocalDate.of(2019, 6, 30));
        asset.setWarrantyProvider("Fixture Supplier");
        asset.setWarrantyReference("W-1");
        asset.setWarrantyExpiryDate(LocalDate.of(2027, 6, 30));
        asset.setCreatedBy(officer.getId());
        assets.save(asset);

        entityManager.flush();
        entityManager.clear();

        Asset loaded = assets.findById(asset.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(AssetStatus.BROKEN);
        assertThat(loaded.getCondition()).isEqualTo(AssetCondition.FAIR);
        assertThat(loaded.getLatitude()).isEqualByComparingTo("-12.345678");
        assertThat(loaded.getLongitude()).isEqualByComparingTo("123.456789");
        assertThat(loaded.getInstallationDate()).isEqualTo(LocalDate.of(2019, 6, 30));
        assertThat(loaded.getWarrantyExpiryDate()).isEqualTo(LocalDate.of(2027, 6, 30));
        assertThat(loaded.getWardId()).isEqualTo(ward.getId());
        assertThat(loaded.getLocalBodyId()).isEqualTo(body.getId());
        assertThat(loaded.getAssetCategoryId()).isEqualTo(category.getId());
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();

        Object storedCondition = entityManager
                .createNativeQuery("select condition::text from asset where id = :id")
                .setParameter("id", asset.getId())
                .getSingleResult();
        assertThat(storedCondition).isEqualTo("FAIR");
    }

    @Test
    void appliesV1DefaultsForStatusConditionAndPhotoOrder() {
        LocalBody body = PersistenceFixtures.localBody(entityManager);
        UserAccount officer = PersistenceFixtures.user(entityManager, Role.OFFICER, body.getId());
        Asset asset = PersistenceFixtures.asset(entityManager, PersistenceFixtures.ward(entityManager, body),
                body, PersistenceFixtures.assetCategory(entityManager, body.getId()), officer);
        MediaObject media = PersistenceFixtures.mediaObject(entityManager, MediaPurpose.ASSET_PHOTO, officer.getId());

        AssetPhoto photo = new AssetPhoto();
        photo.setAsset(asset);
        photo.setMediaObjectId(media.getId());
        photo.setUploadedBy(officer.getId());
        assetPhotos.save(photo);

        entityManager.flush();
        entityManager.clear();

        Asset loadedAsset = assets.findById(asset.getId()).orElseThrow();
        assertThat(loadedAsset.getStatus()).isEqualTo(AssetStatus.WORKING);
        assertThat(loadedAsset.getCondition()).isEqualTo(AssetCondition.GOOD);

        AssetPhoto loadedPhoto = assetPhotos.findById(photo.getId()).orElseThrow();
        assertThat(loadedPhoto.getAsset().getId()).isEqualTo(asset.getId());
        assertThat(loadedPhoto.getMediaObjectId()).isEqualTo(media.getId());
        assertThat(loadedPhoto.getSortOrder()).isZero();
        assertThat(loadedPhoto.getUploadedAt()).isNotNull();
    }
}
