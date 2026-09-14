package com.cams.backend.testsupport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.postgresql.util.PSQLException;

import com.cams.backend.asset.Asset;
import com.cams.backend.assetcategory.AssetCategory;
import com.cams.backend.auth.UserAccount;
import com.cams.backend.common.domain.Role;
import com.cams.backend.complaint.Complaint;
import com.cams.backend.localbody.LocalBody;
import com.cams.backend.localbody.LocalBodyType;
import com.cams.backend.localbody.VillageMunicipality;
import com.cams.backend.localbody.Ward;
import com.cams.backend.media.MediaObject;
import com.cams.backend.media.MediaPurpose;

/**
 * Minimal parent rows for persistence tests, created through the EntityManager
 * because each module's repositories are package-private. Values are synthetic
 * and unique where V1 has a unique constraint.
 */
public final class PersistenceFixtures {

    private PersistenceFixtures() {
    }

    public static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }

    /**
     * Walks the cause chain of a persistence exception to the underlying
     * {@link PSQLException} and returns the name of the PostgreSQL
     * constraint that rejected the statement, so a constraint-violation test
     * can assert *which* rule fired, not just that some exception occurred.
     */
    public static String constraintName(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof PSQLException psqlException && psqlException.getServerErrorMessage() != null) {
                return psqlException.getServerErrorMessage().getConstraint();
            }
        }
        throw new AssertionError(
                "No PSQLException with a server error message found in the cause chain of: " + ex, ex);
    }

    public static LocalBody localBody(EntityManager em) {
        LocalBody body = new LocalBody();
        body.setName("Fixture Local Body");
        body.setCode(unique("LB"));
        body.setType(LocalBodyType.PANCHAYAT);
        em.persist(body);
        return body;
    }

    public static Ward ward(EntityManager em, LocalBody body) {
        VillageMunicipality village = new VillageMunicipality();
        village.setLocalBody(body);
        village.setName(unique("Village"));
        em.persist(village);

        Ward ward = new Ward();
        ward.setVillageMunicipality(village);
        ward.setName("Fixture Ward");
        em.persist(ward);
        return ward;
    }

    public static UserAccount user(EntityManager em, Role role, UUID localBodyId) {
        UserAccount user = new UserAccount();
        user.setRole(role);
        user.setFullName("Fixture " + role);
        user.setLoginIdentifier(unique("login"));
        user.setPasswordHash("fixture-not-a-real-hash");
        user.setLocalBodyId(localBodyId);
        em.persist(user);
        return user;
    }

    public static AssetCategory assetCategory(EntityManager em, UUID localBodyId) {
        AssetCategory category = new AssetCategory();
        category.setLocalBodyId(localBodyId);
        category.setName(unique("Category"));
        em.persist(category);
        return category;
    }

    public static MediaObject mediaObject(EntityManager em, MediaPurpose purpose, UUID uploadedBy) {
        MediaObject media = new MediaObject();
        media.setStorageKey(unique("fixture/media") + ".jpg");
        media.setContentType("image/jpeg");
        media.setSizeBytes(2048L);
        media.setChecksum("0".repeat(64));
        media.setPurpose(purpose);
        media.setUploadedBy(uploadedBy);
        em.persist(media);
        return media;
    }

    public static Asset asset(EntityManager em, Ward ward, LocalBody body, AssetCategory category,
            UserAccount createdBy) {
        Asset asset = new Asset();
        asset.setWardId(ward.getId());
        asset.setLocalBodyId(body.getId());
        asset.setAssetCategoryId(category.getId());
        asset.setPublicCode(unique("AST"));
        asset.setLatitude(new BigDecimal("23.022505"));
        asset.setLongitude(new BigDecimal("72.571365"));
        asset.setInstallationDate(LocalDate.of(2020, 1, 15));
        asset.setCreatedBy(createdBy.getId());
        em.persist(asset);
        return asset;
    }

    public static Complaint complaint(EntityManager em, Asset asset, UserAccount citizen) {
        Complaint complaint = new Complaint();
        complaint.setAssetId(asset.getId());
        complaint.setWardId(asset.getWardId());
        complaint.setLocalBodyId(asset.getLocalBodyId());
        complaint.setReportedBy(citizen.getId());
        complaint.setDescription("Fixture complaint");
        em.persist(complaint);
        return complaint;
    }

    /** Convenience: persists a full LocalBody → Ward → Asset → Complaint chain. */
    public static Complaint complaintWithParents(EntityManager em) {
        LocalBody body = localBody(em);
        Ward ward = ward(em, body);
        UserAccount officer = user(em, Role.OFFICER, body.getId());
        UserAccount citizen = user(em, Role.CITIZEN, body.getId());
        Asset asset = asset(em, ward, body, assetCategory(em, body.getId()), officer);
        return complaint(em, asset, citizen);
    }
}
