package com.cams.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.common.domain.Role;
import com.cams.backend.localbody.LocalBody;
import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class AuthPersistenceTest {

    @Autowired
    private UserAccountRepository users;

    @Autowired
    private RefreshTokenRepository refreshTokens;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsUserAccountWithEnumsDefaultsAndCreator() {
        LocalBody body = PersistenceFixtures.localBody(entityManager);
        UserAccount admin = PersistenceFixtures.user(entityManager, Role.ADMIN, null);

        UserAccount officer = new UserAccount();
        officer.setRole(Role.OFFICER);
        officer.setFullName("Test Officer");
        officer.setLoginIdentifier(PersistenceFixtures.unique("officer"));
        officer.setMobileNumber("9000000000");
        officer.setPasswordHash("fixture-not-a-real-hash");
        officer.setPreferredLanguage(Language.HI);
        officer.setLocalBodyId(body.getId());
        officer.setCreatedBy(admin);
        users.save(officer);

        entityManager.flush();
        entityManager.clear();

        UserAccount loaded = users.findById(officer.getId()).orElseThrow();
        assertThat(loaded.getRole()).isEqualTo(Role.OFFICER);
        assertThat(loaded.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(loaded.getPreferredLanguage()).isEqualTo(Language.HI);
        assertThat(loaded.getLocalBodyId()).isEqualTo(body.getId());
        assertThat(loaded.getCreatedBy().getId()).isEqualTo(admin.getId());
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(loaded.getDeactivatedAt()).isNull();

        UserAccount loadedAdmin = users.findById(admin.getId()).orElseThrow();
        assertThat(loadedAdmin.getLocalBodyId()).isNull();
        assertThat(loadedAdmin.getPreferredLanguage()).isEqualTo(Language.EN);
    }

    @Test
    void persistsDeactivationFields() {
        UserAccount admin = PersistenceFixtures.user(entityManager, Role.ADMIN, null);
        UserAccount worker = PersistenceFixtures.user(entityManager, Role.WORKER,
                PersistenceFixtures.localBody(entityManager).getId());

        worker.setStatus(AccountStatus.INACTIVE);
        worker.setDeactivatedAt(Instant.now());
        worker.setDeactivatedBy(admin);
        entityManager.flush();
        entityManager.clear();

        UserAccount loaded = users.findById(worker.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(AccountStatus.INACTIVE);
        assertThat(loaded.getDeactivatedAt()).isNotNull();
        assertThat(loaded.getDeactivatedBy().getId()).isEqualTo(admin.getId());
    }

    @Test
    void persistsRefreshTokenRotationChain() {
        UserAccount citizen = PersistenceFixtures.user(entityManager, Role.CITIZEN,
                PersistenceFixtures.localBody(entityManager).getId());

        RefreshToken first = refreshTokens.save(token(citizen));
        RefreshToken second = refreshTokens.save(token(citizen));
        first.setRevokedAt(Instant.now());
        first.setReplacedBy(second);

        entityManager.flush();
        entityManager.clear();

        RefreshToken loaded = refreshTokens.findById(first.getId()).orElseThrow();
        assertThat(loaded.getUserAccount().getId()).isEqualTo(citizen.getId());
        assertThat(loaded.getReplacedBy().getId()).isEqualTo(second.getId());
        assertThat(loaded.getIssuedAt()).isNotNull();
        assertThat(loaded.getExpiresAt()).isAfter(loaded.getIssuedAt());
        assertThat(loaded.getRevokedAt()).isNotNull();
        assertThat(refreshTokens.findById(second.getId()).orElseThrow().getReplacedBy()).isNull();
    }

    private static RefreshToken token(UserAccount owner) {
        RefreshToken token = new RefreshToken();
        token.setUserAccount(owner);
        token.setTokenHash(PersistenceFixtures.unique("hash"));
        token.setExpiresAt(Instant.now().plus(14, ChronoUnit.DAYS));
        token.setDeviceLabel("fixture device");
        return token;
    }
}
