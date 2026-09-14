package com.cams.backend.maintenance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.cams.backend.asset.AssetCondition;
import com.cams.backend.asset.AssetStatus;
import com.cams.backend.auth.UserAccount;
import com.cams.backend.common.domain.Role;
import com.cams.backend.complaint.Complaint;
import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class MaintenancePersistenceTest {

    @Autowired
    private WorkerAssignmentRepository assignments;

    @Autowired
    private MaintenanceHistoryRepository maintenanceHistory;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsAssignmentWithDefaults() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        WorkerAssignment assignment = assignments.save(assignment(complaint, worker(complaint), officer(complaint)));
        entityManager.flush();
        entityManager.clear();

        WorkerAssignment loaded = assignments.findById(assignment.getId()).orElseThrow();
        assertThat(loaded.isActive()).isTrue();
        assertThat(loaded.getAssignedAt()).isNotNull();
        assertThat(loaded.getUnassignedAt()).isNull();
        assertThat(loaded.getComplaintId()).isEqualTo(complaint.getId());
    }

    @Test
    void databaseRejectsASecondActiveAssignmentForTheSameComplaint() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        UserAccount officer = officer(complaint);
        assignments.saveAndFlush(assignment(complaint, worker(complaint), officer));

        WorkerAssignment second = assignment(complaint, worker(complaint), officer);

        assertThatThrownBy(() -> assignments.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(ex -> assertThat(PersistenceFixtures.constraintName(ex))
                        .isEqualTo("uq_worker_assignment_one_active_per_complaint"));
    }

    @Test
    void allowsReassignmentOnceThePreviousAssignmentIsInactive() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        UserAccount officer = officer(complaint);
        WorkerAssignment first = assignments.saveAndFlush(assignment(complaint, worker(complaint), officer));

        first.setActive(false);
        first.setUnassignedAt(Instant.now());
        assignments.saveAndFlush(first);

        WorkerAssignment second = assignment(complaint, worker(complaint), officer);
        second.setReason("Reassigned to nearer worker");
        assignments.saveAndFlush(second);
        entityManager.clear();

        assertThat(assignments.findById(first.getId()).orElseThrow().isActive()).isFalse();
        assertThat(assignments.findById(second.getId()).orElseThrow().isActive()).isTrue();
    }

    @Test
    void persistsMaintenanceHistoryCostEnumsAndCompletionPhotoIdArray() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        List<UUID> photoIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        MaintenanceHistory history = new MaintenanceHistory();
        history.setAssetId(complaint.getAssetId());
        history.setComplaintId(complaint.getId());
        history.setWorkSummary("Replaced bulb");
        history.setRemarks("Pole repainted");
        history.setRepairCost(new BigDecimal("1250.50"));
        history.setWorkerId(worker(complaint).getId());
        history.setResultingStatus(AssetStatus.WORKING);
        history.setResultingCondition(AssetCondition.FAIR);
        history.setCompletionPhotoIds(photoIds);
        maintenanceHistory.save(history);

        entityManager.flush();
        entityManager.clear();

        MaintenanceHistory loaded = maintenanceHistory.findById(history.getId()).orElseThrow();
        assertThat(loaded.getRepairCost()).isEqualByComparingTo("1250.50");
        assertThat(loaded.getResultingStatus()).isEqualTo(AssetStatus.WORKING);
        assertThat(loaded.getResultingCondition()).isEqualTo(AssetCondition.FAIR);
        assertThat(loaded.getCompletionPhotoIds()).containsExactlyElementsOf(photoIds);
        assertThat(loaded.getCreatedAt()).isNotNull();
    }

    @Test
    void completionPhotoIdsDefaultToEmptyArray() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);

        MaintenanceHistory history = new MaintenanceHistory();
        history.setAssetId(complaint.getAssetId());
        history.setComplaintId(complaint.getId());
        history.setWorkSummary("Inspection only");
        history.setRepairCost(BigDecimal.ZERO);
        history.setWorkerId(worker(complaint).getId());
        history.setResultingStatus(AssetStatus.WORKING);
        history.setResultingCondition(AssetCondition.GOOD);
        maintenanceHistory.save(history);

        entityManager.flush();
        entityManager.clear();

        assertThat(maintenanceHistory.findById(history.getId()).orElseThrow().getCompletionPhotoIds()).isEmpty();
    }

    private UserAccount worker(Complaint complaint) {
        return PersistenceFixtures.user(entityManager, Role.WORKER, complaint.getLocalBodyId());
    }

    private UserAccount officer(Complaint complaint) {
        return PersistenceFixtures.user(entityManager, Role.OFFICER, complaint.getLocalBodyId());
    }

    private static WorkerAssignment assignment(Complaint complaint, UserAccount worker, UserAccount officer) {
        WorkerAssignment assignment = new WorkerAssignment();
        assignment.setComplaintId(complaint.getId());
        assignment.setWorkerId(worker.getId());
        assignment.setAssignedBy(officer.getId());
        return assignment;
    }
}
