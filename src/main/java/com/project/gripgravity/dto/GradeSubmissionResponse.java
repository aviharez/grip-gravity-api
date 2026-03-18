package com.project.gripgravity.dto;

import com.project.gripgravity.model.Grade;
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
@Schema(description = "Response payload representing a community grade submission")
public class GradeSubmissionResponse {

    @Schema(description = "Unique identifier of the submission", example = "42")
    private Long id;

    @Schema(description = "ID of the route this submission belongs to", example = "7")
    private Long routeId;

    @Schema(description = "The V-scale grade suggested by the climber", example = "V4")
    private Grade suggestedGrade;

    @Schema(description = "Name of the climber (may be null)", example = "Jane Doe")
    private String climberName;

    @Schema(description = "Timestamp when the submission was recorded")
    private LocalDateTime submittedAt;

}
