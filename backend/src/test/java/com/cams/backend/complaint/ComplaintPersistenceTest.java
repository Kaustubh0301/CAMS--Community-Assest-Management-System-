package com.cams.backend.complaint;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.cams.backend.common.domain.Role;
import com.cams.backend.media.MediaObject;
import com.cams.backend.media.MediaPurpose;
import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class ComplaintPersistenceTest {

    @Autowired
    private ComplaintRepository complaints;

    @Autowired
    private ComplaintPhotoRepository complaintPhotos;

    @Autowired
    private ComplaintStatusHistoryRepository statusHistory;

    @Autowired
    private EntityManager entityManager;

    @Test
    void newComplaintStartsPendingWithNoPriorityAndZeroReturns() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        entityManager.flush();
        entityManager.clear();

        Complaint loaded = complaints.findById(complaint.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(ComplaintStatus.PENDING);
        assertThat(loaded.getPriority()).isNull();
        assertThat(loaded.getReturnedCount()).isZero();
        assertThat(loaded.getCurrentAssignmentId()).isNull();
        assertThat(loaded.getRejectedReason()).isNull();
        assertThat(loaded.getSubmittedAt()).isNotNull();
        assertThat(loaded.getUpdatedAt()).isNotNull();
        assertThat(loaded.getClosedAt()).isNull();
    }

    @Test
    void persistsStatusPriorityAndReturnedCountAsNativeValues() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setPriority(ComplaintPriority.CRITICAL);
        complaint.setReturnedCount(2);
        entityManager.flush();
        entityManager.clear();

        Complaint loaded = complaints.findById(complaint.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
        assertThat(loaded.getPriority()).isEqualTo(ComplaintPriority.CRITICAL);
        assertThat(loaded.getReturnedCount()).isEqualTo(2);

        Object[] stored = (Object[]) entityManager
                .createNativeQuery("select status::text, priority::text from complaint where id = :id")
                .setParameter("id", complaint.getId())
                .getSingleResult();
        assertThat(stored).containsExactly("IN_PROGRESS", "CRITICAL");
    }

    @Test
    void persistsCompletionPhotoAndAppendOnlyReturnEvent() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        var worker = PersistenceFixtures.user(entityManager, Role.WORKER, complaint.getLocalBodyId());
        var officer = PersistenceFixtures.user(entityManager, Role.OFFICER, complaint.getLocalBodyId());
        MediaObject media = PersistenceFixtures.mediaObject(entityManager, MediaPurpose.COMPLAINT_PHOTO, worker.getId());

        ComplaintPhoto photo = new ComplaintPhoto();
        photo.setComplaint(complaint);
        photo.setMediaObjectId(media.getId());
        photo.setKind(ComplaintPhotoKind.WORKER_COMPLETION);
        photo.setUploadedBy(worker.getId());
        complaintPhotos.save(photo);

        ComplaintStatusHistory returned = statusHistory.save(new ComplaintStatusHistory(complaint,
                ComplaintStatus.RESOLVED, ComplaintStatus.IN_PROGRESS, ComplaintEvent.RETURN,
                officer.getId(), Role.OFFICER, "Light still flickering"));

        entityManager.flush();
        entityManager.clear();

        ComplaintPhoto loadedPhoto = complaintPhotos.findById(photo.getId()).orElseThrow();
        assertThat(loadedPhoto.getKind()).isEqualTo(ComplaintPhotoKind.WORKER_COMPLETION);
        assertThat(loadedPhoto.getComplaint().getId()).isEqualTo(complaint.getId());

        ComplaintStatusHistory loadedEvent = statusHistory.findById(returned.getId()).orElseThrow();
        assertThat(loadedEvent.getEventType()).isEqualTo(ComplaintEvent.RETURN);
        assertThat(loadedEvent.getFromStatus()).isEqualTo(ComplaintStatus.RESOLVED);
        assertThat(loadedEvent.getToStatus()).isEqualTo(ComplaintStatus.IN_PROGRESS);
        assertThat(loadedEvent.getActorId()).isEqualTo(officer.getId());
        assertThat(loadedEvent.getActorRole()).isEqualTo(Role.OFFICER);
        assertThat(loadedEvent.getNote()).isEqualTo("Light still flickering");
        assertThat(loadedEvent.getOccurredAt()).isNotNull();
    }

    @Test
    void submitEventMayHaveNoFromStatus() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        ComplaintStatusHistory submit = statusHistory.save(new ComplaintStatusHistory(complaint, null,
                ComplaintStatus.PENDING, ComplaintEvent.SUBMIT, complaint.getReportedBy(), Role.CITIZEN, null));
        entityManager.flush();
        entityManager.clear();

        ComplaintStatusHistory loaded = statusHistory.findById(submit.getId()).orElseThrow();
        assertThat(loaded.getFromStatus()).isNull();
        assertThat(loaded.getToStatus()).isEqualTo(ComplaintStatus.PENDING);
        assertThat(loaded.getNote()).isNull();
    }
}
