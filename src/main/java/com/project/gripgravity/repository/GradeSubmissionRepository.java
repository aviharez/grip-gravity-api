package com.project.gripgravity.repository;

import com.project.gripgravity.model.GradeSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeSubmissionRepository extends JpaRepository<GradeSubmission, Long> {

    /**
     * Find all grade submissions for a given route, ordered by submission time ascending.
     */
    List<GradeSubmission> findByRouteIdOrderBySubmittedAtAsc(Long routeId);
}
