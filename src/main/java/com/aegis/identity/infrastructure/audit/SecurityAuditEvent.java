package com.aegis.identity.infrastructure.audit;

import java.time.Instant;
import java.util.UUID;

public record SecurityAuditEvent(
    Instant timestamp,
    AuditEventType eventType,
    UUID userId,
    UUID organizationId,
    UUID workspaceId,
    String ipAddress,
    String userAgent,
    String status,
    String details) {
  public static SecurityAuditEvent of(
      AuditEventType eventType, UUID userId, String status, String details) {
    return new SecurityAuditEvent(
        Instant.now(), eventType, userId, null, null, null, null, status, details);
  }
}
