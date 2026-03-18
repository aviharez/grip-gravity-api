package com.project.gripgravity.dto;

import com.project.gripgravity.model.Grade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for submitting a community grade")
public class GradeSubmissionRequest {

    @NotNull(message = "Suggested grade must not be null")
    @Schema(description = "The V-scale grade the climber suggests for this route", example = "V4")
    private Grade suggestedGrade;

    @Schema(description = "Optional name of the climber submitting the grade", example = "Jane Doe")
    private String climberName;

}
