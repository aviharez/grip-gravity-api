package com.project.gripgravity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Freshness score for a wall section based on active route ages")
public class FreshnessResponse {

    @Schema(description = "Wall section identifier", example = "CAVE")
    private String wallSection;

    @Schema(description = "Freshness score from 0.00 to 100.00 (higher = newer routes)", example = "78.57")
    private double freshnessScore;

    @Schema(description = "Number of currently active routes in this section", example = "5")
    private int activeRouteCount;

    @Schema(description = "Average age in days of the active routes in this section", example = "14.50")
    private double averageAgeDays;

}
