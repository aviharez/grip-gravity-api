package com.project.gripgravity.dto;

import com.project.gripgravity.model.Grade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for creating or updating a route")
public class RouteRequest {

    @NotBlank(message = "Route name must not be blank")
    @Schema(description = "Name of the route", example = "The Crimson Slab")
    private String name;

    @NotBlank(message = "Color must not be blank")
    @Schema(description = "Tape color used to mark the route holds", example = "RED")
    private String color;

    @NotBlank(message = "Wall section must not be blank")
    @Schema(description = "Wall section identifier, e.g. A, B, CAVE", example = "CAVE")
    private String wallSection;

    @NotBlank(message = "Setter name must not be blank")
    @Schema(description = "Name of the route setter", example = "Alex Honnold")
    private String setterName;

    @NotNull(message = "Official grade must not be null")
    @Schema(description = "Official V-scale grade assigned by the setter", example = "V5")
    private Grade officialGrade;

    @Schema(description = "Optional notes about the route or its holds", example = "Dynamic move at the top")
    private String notes;

}
