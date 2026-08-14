package com.aegis.identity.infrastructure.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SecurityAuditLogger {

    private static final Logger log = LoggerFactory.getLogger("SECURITY_AUDIT");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void logEvent(SecurityAuditEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info(json);
        } catch (Exception ex) {
            log.info("SECURITY_AUDIT eventType={} userId={} status={} details={}",
                    event.eventType(), event.userId(), event.status(), event.details());
        }
    }
}
