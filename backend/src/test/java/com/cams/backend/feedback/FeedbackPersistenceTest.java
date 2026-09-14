package com.cams.backend.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.cams.backend.complaint.Complaint;
import com.cams.backend.testsupport.CamsDataJpaTest;
import com.cams.backend.testsupport.PersistenceFixtures;

@CamsDataJpaTest
class FeedbackPersistenceTest {

    @Autowired
    private FeedbackRepository feedbacks;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistsRatingAndOneTimeText() {
        // Rating and text are both set before the row is created: FR-FEED-004
        // says feedback is final once submitted, so this test must not edit
        // it after save (that would demonstrate an update the requirement
        // forbids, not just persistence).
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        Feedback feedback = feedback(complaint, 4);
        feedback.setFeedbackText("Fixed quickly");
        feedbacks.save(feedback);
        entityManager.flush();
        entityManager.clear();

        Feedback loaded = feedbacks.findById(feedback.getId()).orElseThrow();
        assertThat(loaded.getRatingValue()).isEqualTo(4);
        assertThat(loaded.getFeedbackText()).isEqualTo("Fixed quickly");
        assertThat(loaded.getCitizenId()).isEqualTo(complaint.getReportedBy());
        assertThat(loaded.getSubmittedAt()).isNotNull();
    }

    @Test
    void citizenIdIsNotUpdatedAfterInsert() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        Feedback feedback = feedbacks.saveAndFlush(feedback(complaint, 5));
        UUID originalCitizenId = feedback.getCitizenId();
        entityManager.clear();

        Feedback managed = feedbacks.findById(feedback.getId()).orElseThrow();
        managed.setCitizenId(UUID.randomUUID());
        entityManager.flush();
        entityManager.clear();

        assertThat(feedbacks.findById(feedback.getId()).orElseThrow().getCitizenId())
                .isEqualTo(originalCitizenId);
    }

    @Test
    void databaseRejectsRatingBelowOne() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);

        assertThatThrownBy(() -> feedbacks.saveAndFlush(feedback(complaint, 0)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(ex -> assertThat(PersistenceFixtures.constraintName(ex))
                        .isEqualTo("chk_feedback_rating_value_min"));
    }

    @Test
    void databaseRejectsSecondFeedbackForTheSameComplaint() {
        Complaint complaint = PersistenceFixtures.complaintWithParents(entityManager);
        feedbacks.saveAndFlush(feedback(complaint, 5));

        assertThatThrownBy(() -> feedbacks.saveAndFlush(feedback(complaint, 3)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(ex -> assertThat(PersistenceFixtures.constraintName(ex))
                        .isEqualTo("uq_feedback_complaint"));
    }

    private static Feedback feedback(Complaint complaint, int rating) {
        Feedback feedback = new Feedback();
        feedback.setComplaintId(complaint.getId());
        feedback.setCitizenId(complaint.getReportedBy());
        feedback.setRatingValue(rating);
        return feedback;
    }
}
