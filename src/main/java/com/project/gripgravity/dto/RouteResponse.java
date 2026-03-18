package com.project.gripgravity.dto;

import com.project.gripgravity.model.Grade;
import com.project.gripgravity.model.RouteStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response payload representing a route")
public class RouteResponse {

    @Schema(description = "Unique identifier of the route", example = "1")
    private Long id;

    @Schema(description = "Name of the route", example = "The Crimson Slab")
    private String name;

    @Schema(description = "Tape color used to mark the route holds", example = "RED")
    private String color;

    @Schema(description = "Wall section where the route is located", example = "CAVE")
    private String wallSection;

    @Schema(description = "Name of the route setter", example = "Alex Honnold")
    private String setterName;

    @Schema(description = "Official grade assigned by the setter", example = "V5")
    private Grade officialGrade;

    @Schema(description = "Community-voted grade (null if no submissions)", example = "V4")
    private Grade communityGrade;

    @Schema(description = "Current lifecycle status of the route", example = "ACTIVE")
    private RouteStatus status;

    @Schema(description = "Timestamp when the route was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the route was first activated (null if never activated)")
    private LocalDateTime activatedAt;

    @Schema(description = "Optional notes about the route", example = "Dynamic move at the top")
    private String notes;

}
