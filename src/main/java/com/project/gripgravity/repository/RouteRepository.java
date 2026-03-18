package com.project.gripgravity.repository;

import com.project.gripgravity.model.Route;
import com.project.gripgravity.model.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /**
     * Find all routes with the given status
     */
    List<Route> findByStatus(RouteStatus status);

    /**
     * Find all routes in a given wall section with a specific status.
     */
    List<Route> findByWallSectionAndStatus(String wallSection, RouteStatus status);

    /**
     * Find all routes grouped by wall section - only active ones.
     * Used for freshness calculation
     */
    List<Route> findByStatusOrderByWallSectionAsc(RouteStatus status);

}
