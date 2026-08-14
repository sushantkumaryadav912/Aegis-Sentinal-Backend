package com.aegis.identity.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Standardized error response payload for API errors")
public record ApiErrorResponse(
    @Schema(description = "HTTP status code integer", example = "400") int status,
    @Schema(description = "Short error title or HTTP status phrase", example = "Bad Request")
        String error,
    @Schema(
            description = "Detailed error message",
            example = "Invalid request payload or failed validation")
        String message,
    @Schema(
            description = "Request URI path where error occurred",
            example = "/api/aegis/v1/auth/login")
        String path,
    @Schema(
            description = "ISO-8601 UTC timestamp of error occurrence",
            example = "2026-08-15T00:00:00Z")
        String timestamp,
    @Schema(
            description = "Field-level validation error mapping (if applicable)",
            example = "{\"email\": \"must not be blank\"}")
        Map<String, String> fieldErrors) {

  public ApiErrorResponse(int status, String error, String message, String path) {
    this(status, error, message, path, Instant.now().toString(), null);
  }

  public ApiErrorResponse(
      int status, String error, String message, String path, Map<String, String> fieldErrors) {
    this(status, error, message, path, Instant.now().toString(), fieldErrors);
  }
}
