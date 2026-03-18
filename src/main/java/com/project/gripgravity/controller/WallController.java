package com.project.gripgravity.controller;

import com.project.gripgravity.dto.ErrorResponse;
import com.project.gripgravity.dto.FreshnessResponse;
import com.project.gripgravity.service.WallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/walls")
@RequiredArgsConstructor
@Tag(name = "Walls", description = "Query wall section freshness based on active route age")
public class WallController {

    private final WallService wallService;

    @GetMapping("/freshness")
    @Operation(
            summary = "Get freshness for all wall sections",
            description = "Returns freshness scores for every wall section that has at least one active route. Freshness = 100 - (avgAgeDays / 7) * 100, clamped to [0, 100]"
    )
    @ApiResponse(responseCode = "200", description = "Freshness data returned for all sections")
    public ResponseEntity<List<FreshnessResponse>> getFreshnessAllSections() {
        return ResponseEntity.ok(wallService.getFreshnessAllSections());
    }

    @GetMapping("/{section}/freshness")
    @Operation(
            summary = "Get freshness for a specific wall section",
            description = "Returns the freshness score for the given wall section. Returns score=0 if the section has no active routes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Freshness data returned"),
            @ApiResponse(responseCode = "400", description = "Invalid section parameter", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<FreshnessResponse> getFreshnessForSection(
            @Parameter(description = "Wall section identifier, e.g. A, B, CAVE") @PathVariable String section) {
        return ResponseEntity.ok(wallService.getFreshnessForSection(section));
    }

}
