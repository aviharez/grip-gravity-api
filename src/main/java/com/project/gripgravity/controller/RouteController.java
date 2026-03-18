package com.project.gripgravity.controller;

import com.project.gripgravity.dto.*;
import com.project.gripgravity.model.RouteStatus;
import com.project.gripgravity.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
@Tag(name = "Routes", description = "Manage bouldering routes: lifecycle, grades, and metadata")
public class RouteController {

    private final RouteService routeService;

    // CRUD

    @GetMapping
    @Operation(summary = "List all routes", description = "Returns all routes, optionally filtered by status")
    @ApiResponse(responseCode = "200", description = "List of routes returned successfully")
    public ResponseEntity<List<RouteResponse>> getAllRoutes(
            @Parameter(description = "Filter by route status (DRAFT, ACTIVE, MAINTENANCE, RETIRED)")
            @RequestParam(required = false) RouteStatus status) {
        return ResponseEntity.ok(routeService.getAllRoutes(status));
    }

    @PostMapping
    @Operation(summary = "Create a route", description = "Creates a new route in DRAFT status")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Route created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse created = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route by ID", description = "Returns a single route by its numeric ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route found"),
            @ApiResponse(responseCode = "404", description = "Route not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RouteResponse> getRouteById(@Parameter(description = "Route ID") @PathVariable Long id) {
        return ResponseEntity.ok(routeService.getRouteById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a route", description = "Updates an existing DRAFT or ACTIVE route")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "422", description = "Route is RETIRED and cannot be updated", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RouteResponse> updateRoute(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Valid @RequestBody RouteRequest request) {
        return ResponseEntity.ok(routeService.updateRoute(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a route", description = "Permanently deletes a DRAFT route")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Route deleted"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "422", description = "Only DRAFT routes can be deleted", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteRoute(@Parameter(description = "Route ID") @PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    // State Transition

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a route", description = "Transitions a DRAFT route to ACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route activated"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "422", description = "Invalid state transition", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RouteResponse> activate(@Parameter(description = "Route ID") @PathVariable Long id) {
        return ResponseEntity.ok(routeService.activate(id));
    }

    @PostMapping("/{id}/maintenance")
    @Operation(summary = "Send route to maintenance", description = "Transitions an ACTIVE route to MAINTENANCE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route sent to maintenance"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "422", description = "Invalid state transition", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RouteResponse> sendToMaintenance(@Parameter(description = "Route ID") @PathVariable Long id) {
        return ResponseEntity.ok(routeService.sendToMaintenance(id));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore route from maintenance", description = "Transitions a MAINTENANCE route back to ACTIVE")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route restored to active"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "422", description = "Invalid state transition", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RouteResponse> restore(@Parameter(description = "Route ID") @PathVariable Long id) {
        return ResponseEntity.ok(routeService.restore(id));
    }

    @PostMapping("/{id}/retire")
    @Operation(summary = "Retire a route", description = "Transitions an ACTIVE or MAINTENANCE route to RETIRED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Route retired"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "422", description = "Invalid state transition", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RouteResponse> retire(@Parameter(description = "Route ID") @PathVariable Long id) {
        return ResponseEntity.ok(routeService.retire(id));
    }

    @PostMapping("/{id}/reset")
    @Operation(summary = "Reset a retired route", description = "Creates a new DRAFT route based on a RETIRED route. The original stays RETIRED.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "New DRAFT route created from retired route"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "422", description = "Route is not RETIRED", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RouteResponse> reset(@Parameter(description = "Route ID of the RETIRED route") @PathVariable Long id) {
        RouteResponse newDraft = routeService.reset(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDraft);
    }

    // Grade Submissions

    @PostMapping("/{id}/grades")
    @Operation(summary = "Submit a community grade", description = "Submit a grade suggestion for an ACTIVE or MAINTENANCE route. The community grade is recomputed as the average of all submissions.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Grade submission recorded"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Route not found"),
            @ApiResponse(responseCode = "422", description = "Route must be ACTIVE or MAINTENANCE", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<GradeSubmissionResponse> submitGrade(
            @Parameter(description = "Route ID") @PathVariable Long id,
            @Valid @RequestBody GradeSubmissionRequest request) {
        GradeSubmissionResponse response = routeService.submitGrade(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/grades")
    @Operation(summary = "List grade submissions", description = "Returns all community grade submissions for a route, ordered by submission time")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Submissions returned"),
            @ApiResponse(responseCode = "404", description = "Route not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<GradeSubmissionResponse>> getGradeSubmissions(@Parameter(description = "Route ID") @PathVariable Long id) {
        return ResponseEntity.ok(routeService.getGradeSubmissions(id));
    }

}
