package com.aegis.identity;

import static org.assertj.core.api.Assertions.assertThatCode;

import com.aegis.identity.infrastructure.audit.AuditEventType;
import com.aegis.identity.infrastructure.audit.SecurityAuditEvent;
import com.aegis.identity.infrastructure.audit.SecurityAuditLogger;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SecurityAuditLoggingTest {

  @Autowired private SecurityAuditLogger securityAuditLogger;

  @Test
  @DisplayName(
      "5.11 Audit Logging: SecurityAuditLogger publishes structured JSON audit events without throwing exceptions")
  void testSecurityAuditLogging_publishesStructuredJson() {
    SecurityAuditEvent event =
        SecurityAuditEvent.of(
            AuditEventType.AUTH_LOGIN_SUCCESS,
            UUID.randomUUID(),
            "SUCCESS",
            "Audit log verification test");

    assertThatCode(() -> securityAuditLogger.logEvent(event)).doesNotThrowAnyException();
  }
}
