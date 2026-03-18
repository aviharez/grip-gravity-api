package com.project.gripgravity.service;

import com.project.gripgravity.dto.GradeSubmissionRequest;
import com.project.gripgravity.dto.GradeSubmissionResponse;
import com.project.gripgravity.dto.RouteRequest;
import com.project.gripgravity.dto.RouteResponse;
import com.project.gripgravity.exception.InvalidStateTransitionException;
import com.project.gripgravity.model.Grade;
import com.project.gripgravity.model.GradeSubmission;
import com.project.gripgravity.model.Route;
import com.project.gripgravity.model.RouteStatus;
import com.project.gripgravity.repository.GradeSubmissionRepository;
import com.project.gripgravity.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RouteService Unit Test")
public class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private GradeSubmissionRepository gradeSubmissionRepository;

    @InjectMocks
    private RouteService routeService;

    private RouteRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = RouteRequest.builder()
                .name("The Crimson Slab")
                .color("RED")
                .wallSection("SLAB")
                .setterName("Jane Doe")
                .officialGrade(Grade.V4)
                .notes("Fun overhang")
                .build();
    }

    private Route buildRoute(Long id, RouteStatus status, Grade grade, LocalDateTime activatedAt) {
        return Route.builder()
                .id(id)
                .name("The Crimson Slab")
                .color("RED")
                .wallSection("SLAB")
                .setterName("Jane Doe")
                .officialGrade(grade)
                .status(status)
                .createdAt(LocalDateTime.now().minusDays(14))
                .activatedAt(activatedAt)
                .build();
    }

    /**
     * createRoute - should return a DRAFT route
     */
    @Test
    @DisplayName("createRoute - persists and returns a DRAFT route")
    void createRoute_returnsDraftRoute() {
        Route saved = buildRoute(1L, RouteStatus.DRAFT, Grade.V5, null);
        when(routeRepository.save(any(Route.class))).thenReturn(saved);

        RouteResponse response = routeService.createRoute(validRequest);

        assertThat(response.getStatus()).isEqualTo(RouteStatus.DRAFT);
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("The Crimson Slab");
        assertThat(response.getOfficialGrade()).isEqualTo(Grade.V5);
        assertThat(response.getCommunityGrade()).isNull();
        verify(routeRepository).save(any(Route.class));
    }

    /**
     * activate - DRAFT -> ACTIVE, sets activatedAt
     */
    @Test
    @DisplayName("activate - transitions DRAFT to ACTIVE and sets activatedAt")
    void activate_draftToActive_setsActivatedAt() {
        Route draft = buildRoute(2L, RouteStatus.DRAFT, Grade.V3, null);
        when(routeRepository.findById(2L)).thenReturn(Optional.of(draft));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));

        RouteResponse response = routeService.activate(2L);

        assertThat(response.getStatus()).isEqualTo(RouteStatus.ACTIVE);
        assertThat(response.getActivatedAt()).isNotNull();
        verify(routeRepository).save(draft);
    }

    /**
     * activate - throws InvalidStateTransitionException when already ACTIVE
     */
    @Test
    @DisplayName("activate - throws InvalidStateTransitionException when route is already ACTIVE")
    void activate_alreadyActive_throwsInvalidStateTransition() {
        Route active = buildRoute(3L, RouteStatus.ACTIVE, Grade.V4, LocalDateTime.now().minusDays(5));
        when(routeRepository.findById(3L)).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> routeService.activate(3L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("ACTIVE");

        verify(routeRepository, never()).save(any());
    }

    /**
     * retire - ACTIVE -> RETIRED
     */
    @Test
    @DisplayName("retire - transitions ACTIVE route to RETIRED")
    void retire_activeToRetired() {
        Route active = buildRoute(4L, RouteStatus.ACTIVE, Grade.V6, LocalDateTime.now().minusDays(10));
        when(routeRepository.findById(4L)).thenReturn(Optional.of(active));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));

        RouteResponse response = routeService.retire(4L);

        assertThat(response.getStatus()).isEqualTo(RouteStatus.RETIRED);
        verify(routeRepository).save(active);
    }

    /**
     * retire - throws when called on DRAFT
     */
    @Test
    @DisplayName("retire - throws InvalidStateTransitionException when route is DRAFT")
    void retire_draftRoute_throwsInvalidStateTransition() {
        Route draft = buildRoute(5L, RouteStatus.DRAFT, Grade.V2, null);
        when(routeRepository.findById(5L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> routeService.retire(5L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("RETIRED");

        verify(routeRepository, never()).save(any());
    }

    /**
     * reset - RETIRED -> creates new DRAFT (original stays RETIRED)
     */
    @Test
    @DisplayName("reset - creates a new DRAFT route from a RETIRED route, original stays RETIRED")
    void reset_retiredRoute_createsNewDraft() {
        Route retired = buildRoute(6L, RouteStatus.RETIRED, Grade.V7, LocalDateTime.now().minusDays(30));
        Route newDraft = buildRoute(99L, RouteStatus.DRAFT, Grade.V7, null);

        when(routeRepository.findById(6L)).thenReturn(Optional.of(retired));
        when(routeRepository.save(any(Route.class))).thenReturn(newDraft);

        RouteResponse response = routeService.reset(6L);

        assertThat(retired.getStatus()).isEqualTo(RouteStatus.RETIRED);

        assertThat(response.getStatus()).isEqualTo(RouteStatus.DRAFT);
        assertThat(response.getId()).isEqualTo(99L);

        ArgumentCaptor<Route> captor = ArgumentCaptor.forClass(Route.class);
        verify(routeRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(RouteStatus.DRAFT);
        assertThat(captor.getValue().getId()).isNull();
    }

    /**
     * submitGrade - computes communityGrade correctly ([V3, V5] -> V4)
     */
    @Test
    @DisplayName("submitGrade - community grade is average of all submissions")
    void submitGrade_computesCommunityGrade() {
        Route active = buildRoute(7L, RouteStatus.ACTIVE, Grade.V5, LocalDateTime.now().minusDays(3));
        GradeSubmissionRequest request = GradeSubmissionRequest.builder()
                .suggestedGrade(Grade.V5)
                .climberName("Bob")
                .build();

        GradeSubmission savedSubmission = GradeSubmission.builder()
                .id(10L)
                .route(active)
                .suggestedGrade(Grade.V5)
                .climberName("Bob")
                .submittedAt(LocalDateTime.now())
                .build();

        GradeSubmission existingSubmission = GradeSubmission.builder()
                .id(9L)
                .route(active)
                .suggestedGrade(Grade.V3)
                .climberName("Alice")
                .submittedAt(LocalDateTime.now().minusHours(1))
                .build();

        when(routeRepository.findById(7L)).thenReturn(Optional.of(active));
        when(gradeSubmissionRepository.save(any(GradeSubmission.class))).thenReturn(savedSubmission);
        when(gradeSubmissionRepository.findByRouteIdOrderBySubmittedAtAsc(7L)).thenReturn(List.of(existingSubmission, savedSubmission));
        when(routeRepository.save(any(Route.class))).thenAnswer(inv -> inv.getArgument(0));

        GradeSubmissionResponse response = routeService.submitGrade(7L, request);

        assertThat(response.getSuggestedGrade()).isEqualTo(Grade.V5);
        assertThat(active.getCommunityGrade()).isEqualTo(Grade.V4);
    }

    /**
     * submitGrade - throws when route is DRAFT
     */
    @Test
    @DisplayName("SubmitGrade - throws InvalidStateTransitionException when route is DRAFT")
    void submitGrade_draftRoute_throwsInvalidStateTransition() {
        Route draft = buildRoute(8L, RouteStatus.DRAFT, Grade.V4, null);
        GradeSubmissionRequest request = GradeSubmissionRequest.builder()
                .suggestedGrade(Grade.V4)
                .climberName("Charlie")
                .build();

        when(routeRepository.findById(8L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> routeService.submitGrade(8L, request))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("ACTIVE or MAINTENANCE");

        verify(gradeSubmissionRepository, never()).save(any());
    }

    /**
     * deleteRoute - throws 422 when ACTIVE
     */
    @Test
    @DisplayName("deleteRoute - throws InvalidStateTransitionException when route is ACTIVE")
    void deleteRoute_activeRoute_throwsInvalidStateTransition() {
        Route active = buildRoute(9L, RouteStatus.ACTIVE, Grade.V3, LocalDateTime.now().minusDays(7));
        when(routeRepository.findById(9L)).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> routeService.deleteRoute(9L))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("DRAFT");

        verify(routeRepository, never()).delete(any());
    }

}
