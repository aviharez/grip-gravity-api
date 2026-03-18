package com.project.gripgravity.service;

import com.project.gripgravity.dto.FreshnessResponse;
import com.project.gripgravity.model.Route;
import com.project.gripgravity.model.RouteStatus;
import com.project.gripgravity.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WallService {

    private final RouteRepository routeRepository;

    // Public API

    /**
     * Computes freshness for all wall sections that have at least one ACTIVE route.
     */
    @Transactional(readOnly = true)
    public List<FreshnessResponse> getFreshnessAllSections() {
        List<Route> activeRoute = routeRepository.findByStatusOrderByWallSectionAsc(RouteStatus.ACTIVE);

        Map<String, List<Route>> bySection = activeRoute.stream()
                .collect(Collectors.groupingBy(Route::getWallSection));

        return bySection.entrySet().stream()
                .map(e -> computeFreshness(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Computes freshness for a specific wall section.
     * If the section has no active routes, returns a score of 0.
     */
    @Transactional(readOnly = true)
    public FreshnessResponse getFreshnessForSection(String wallSection) {
        List<Route> activeRoutes = routeRepository.findByWallSectionAndStatus(wallSection, RouteStatus.ACTIVE);
        return computeFreshness(wallSection, activeRoutes);
    }

    // Internal

    private FreshnessResponse computeFreshness(String wallSection, List<Route> routes) {
        if (routes == null || routes.isEmpty()) {
            log.debug("Freshness for section '{}': no active routes, score=0.00", wallSection);
            return FreshnessResponse.builder()
                    .wallSection(wallSection)
                    .freshnessScore(0.0)
                    .activeRouteCount(0)
                    .averageAgeDays(0.0)
                    .build();
        }

        LocalDate today = LocalDate.now();
        double totalDays = routes.stream()
                .mapToLong(r -> {
                    if (r.getActivatedAt() == null) {
                        return 0L;
                    }
                    return ChronoUnit.DAYS.between(r.getActivatedAt().toLocalDate(), today);
                })
                .sum();

        double averageAgeDays = totalDays / routes.size();
        double rawScore = 100.0 - (averageAgeDays / 7.0) * 100.0;
        double clampedScore = Math.max(0.0, Math.min(100.0, rawScore));

        // Round to 2 decimal places
        double freshnessScore = Math.round(clampedScore * 100.0) / 100.0;
        double roundedAvgAge = Math.round(averageAgeDays * 100.0) / 100.0;

        log.debug("Freshness for section '{}': activeRoutes:{}, avgAgeDays={}, score={}", wallSection, routes.size(), roundedAvgAge, freshnessScore);

        return FreshnessResponse.builder()
                .wallSection(wallSection)
                .freshnessScore(freshnessScore)
                .activeRouteCount(routes.size())
                .averageAgeDays(roundedAvgAge)
                .build();
    }

}
