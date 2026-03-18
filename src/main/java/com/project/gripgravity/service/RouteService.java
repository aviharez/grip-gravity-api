package com.project.gripgravity.service;

import com.project.gripgravity.dto.GradeSubmissionRequest;
import com.project.gripgravity.dto.GradeSubmissionResponse;
import com.project.gripgravity.dto.RouteRequest;
import com.project.gripgravity.dto.RouteResponse;
import com.project.gripgravity.exception.InvalidStateTransitionException;
import com.project.gripgravity.exception.RouteNotFoundException;
import com.project.gripgravity.model.Grade;
import com.project.gripgravity.model.GradeSubmission;
import com.project.gripgravity.model.Route;
import com.project.gripgravity.model.RouteStatus;
import com.project.gripgravity.repository.GradeSubmissionRepository;
import com.project.gripgravity.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final GradeSubmissionRepository gradeSubmissionRepository;

    // CRUD

    @Transactional
    public RouteResponse createRoute(RouteRequest request) {
        Route route = Route.builder()
                .name(request.getName())
                .color(request.getColor())
                .wallSection(request.getWallSection())
                .setterName(request.getSetterName())
                .officialGrade(request.getOfficialGrade())
                .notes(request.getNotes())
                .status(RouteStatus.DRAFT)
                .build();

        Route saved = routeRepository.save(route);
        log.info("Route created: id={}, name='{}', grade={}", saved.getId(), saved.getName(), saved.getOfficialGrade());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRoutes(RouteStatus status) {
        List<Route> routes = (status != null)
                ? routeRepository.findByStatus(status)
                : routeRepository.findAll();
        return routes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RouteResponse getRouteById(Long id) {
        return toResponse(findRouteOrThrow(id));
    }

    @Transactional
    public RouteResponse updateRoute(Long id, RouteRequest request) {
        Route route = findRouteOrThrow(id);

        if (route.getStatus() == RouteStatus.RETIRED) {
            throw new InvalidStateTransitionException(
                    "Cannot update a RETIRED route. Reset it to DRAFT first."
            );
        }

        route.setName(request.getName());
        route.setColor(request.getColor());
        route.setWallSection(request.getWallSection());
        route.setSetterName(request.getSetterName());
        route.setOfficialGrade(request.getOfficialGrade());
        route.setNotes(request.getNotes());

        Route saved = routeRepository.save(route);
        log.info("Route updated: id={}, name='{}'", saved.getId(), saved.getName());
        return toResponse(saved);
    }

    @Transactional
    public void deleteRoute(Long id) {
        Route route = findRouteOrThrow(id);

        if (route.getStatus() != RouteStatus.DRAFT) {
            throw new InvalidStateTransitionException(
                    "Only DRAFT routes can be deleted. Current status: " + route.getStatus()
            );
        }

        routeRepository.delete(route);
        log.info("Route deleted: id={}", id);
    }

    // State Transitions

    @Transactional
    public RouteResponse activate(Long id) {
        Route route = findRouteOrThrow(id);

        if (route.getStatus() != RouteStatus.DRAFT) {
            log.warn("Invalid transition attempt: {} -> ACTIVE for route id={}", route.getStatus(), id);
            throw new InvalidStateTransitionException(route.getStatus(), RouteStatus.ACTIVE);
        }

        route.setStatus(RouteStatus.ACTIVE);
        if (route.getActivatedAt() == null) {
            route.setActivatedAt(LocalDateTime.now());
        }

        Route saved = routeRepository.save(route);
        log.info("Route activated: id={}, activatedAt={}", saved.getId(), saved.getActivatedAt());
        return toResponse(saved);
    }

    @Transactional
    public RouteResponse sendToMaintenance(Long id) {
        Route route = findRouteOrThrow(id);

        if (route.getStatus() != RouteStatus.ACTIVE) {
            log.warn("Invalid transition attempt: {} -> MAINTENANCE for route id={}", route.getStatus(), id);
            throw new InvalidStateTransitionException(route.getStatus(), RouteStatus.MAINTENANCE);
        }

        route.setStatus(RouteStatus.MAINTENANCE);
        Route saved = routeRepository.save(route);
        log.info("Route sent to maintenance: id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public RouteResponse restore(Long id) {
        Route route = findRouteOrThrow(id);

        if (route.getStatus() != RouteStatus.MAINTENANCE) {
            log.warn("Invalid transition attempt: {} -> ACTIVE for route id={}", route.getStatus(), id);
            throw new InvalidStateTransitionException(route.getStatus(), RouteStatus.ACTIVE);
        }

        route.setStatus(RouteStatus.ACTIVE);
        Route saved = routeRepository.save(route);
        log.info("Route restored to active: id={}", saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public RouteResponse retire(Long id) {
        Route route = findRouteOrThrow(id);

        if (route.getStatus() != RouteStatus.ACTIVE && route.getStatus() != RouteStatus.MAINTENANCE) {
            log.warn("Invalid transition attempt: {} -> RETIRED for route id={}", route.getStatus(), id);
            throw new InvalidStateTransitionException(route.getStatus(), RouteStatus.RETIRED);
        }

        route.setStatus(RouteStatus.RETIRED);
        Route saved = routeRepository.save(route);
        log.info("Route retired: id={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Resets a RETIRED route by creating a new DRAFT copy.
     * The original route remains RETIRED.
     *
     * @param id the ID of the RETIRED route to reset
     * @return the newly DRAFT route
     */
    @Transactional
    public RouteResponse reset(Long id) {
        Route original = findRouteOrThrow(id);

        if (original.getStatus() != RouteStatus.RETIRED) {
            log.warn("Invalid transition attempt: {} -> DRAFT (reset) for route id={}", original.getStatus(), id);
            throw new InvalidStateTransitionException(
                    "Only RETIRED routes can be reset. Current status: " + original.getStatus()
            );
        }

        Route newDraft = Route.builder()
                .name(original.getName())
                .color(original.getColor())
                .wallSection(original.getWallSection())
                .setterName(original.getSetterName())
                .officialGrade(original.getOfficialGrade())
                .notes(original.getNotes())
                .status(RouteStatus.DRAFT)
                .build();

        Route saved = routeRepository.save(newDraft);
        log.info("Route reset: original id={} stays RETIRED, new DRAFT id={}", id, saved.getId());
        return toResponse(saved);
    }

    // Grade Submissions

    @Transactional
    public GradeSubmissionResponse submitGrade(Long routeId, GradeSubmissionRequest request) {
        Route route = findRouteOrThrow(routeId);

        if (route.getStatus() != RouteStatus.ACTIVE && route.getStatus() != RouteStatus.MAINTENANCE) {
            throw new InvalidStateTransitionException(
                    "Grade submissions are only allowed for ACTIVE or MAINTENANCE routes. " +
                            "Current status: " + route.getStatus()
            );
        }

        GradeSubmission submission = GradeSubmission.builder()
                .route(route)
                .suggestedGrade(request.getSuggestedGrade())
                .climberName(request.getClimberName())
                .build();

        GradeSubmission saved = gradeSubmissionRepository.save(submission);
        log.info("Grade submitted: routeId={}, grade={}, climber='{}'", routeId, request.getSuggestedGrade(), request.getClimberName());

        // Recompute community grade from all submissions
        recalculateCommunityGrade(route);

        return toSubmissionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GradeSubmissionResponse> getGradeSubmissions(Long routeId) {
        // ensure route exists
        findRouteOrThrow(routeId);
        return gradeSubmissionRepository.findByRouteIdOrderBySubmittedAtAsc(routeId)
                .stream()
                .map(this::toSubmissionResponse)
                .collect(Collectors.toList());
    }

    // Internal Helpers

    private Route findRouteOrThrow(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new RouteNotFoundException(id));
    }

    private void recalculateCommunityGrade(Route route) {
        List<GradeSubmission> allSubmissions = gradeSubmissionRepository.findByRouteIdOrderBySubmittedAtAsc(route.getId());

        if (allSubmissions.isEmpty()) {
            route.setCommunityGrade(null);
        } else {
            double average = allSubmissions.stream()
                    .mapToInt(s -> s.getSuggestedGrade().numericValue())
                    .average()
                    .orElse(0.0);
            Grade computed = Grade.fromNumeric(average);
            route.setCommunityGrade(computed);
            log.info("Community grade recalculated for routeId={}: {} (avg={})", route.getId(), computed, String.format("%.2f", average));
        }
    }

    // Mappers

    private RouteResponse toResponse(Route route) {
        return RouteResponse.builder()
                .id(route.getId())
                .name(route.getName())
                .color(route.getColor())
                .wallSection(route.getWallSection())
                .setterName(route.getSetterName())
                .officialGrade(route.getOfficialGrade())
                .communityGrade(route.getCommunityGrade())
                .status(route.getStatus())
                .createdAt(route.getCreatedAt())
                .activatedAt(route.getActivatedAt())
                .notes(route.getNotes())
                .build();
    }

    private GradeSubmissionResponse toSubmissionResponse(GradeSubmission submission) {
        return GradeSubmissionResponse.builder()
                .id(submission.getId())
                .routeId(submission.getRoute().getId())
                .suggestedGrade(submission.getSuggestedGrade())
                .climberName(submission.getClimberName())
                .submittedAt(submission.getSubmittedAt())
                .build();
    }

}
