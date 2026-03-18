package com.project.gripgravity.dto;

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
@Schema(description = "Standard error response body")
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Short error description", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message", example = "Route not found with id: 99")
    private String message;

    @Schema(description = "Request path that triggered the error", example = "/api/routes/99")
    private String path;

}
