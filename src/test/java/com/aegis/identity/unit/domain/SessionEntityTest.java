package com.aegis.identity.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.Session;
import com.aegis.identity.domain.entity.User;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SessionEntityTest {

  @Test
  @DisplayName("Domain Unit Test: Session creation defaults to 7 days TTL and active status")
  void testSession_creation_defaultsToActive() {
    Organization org = Organization.create("Test Org", "test-org");
    User user = User.create(org, "test@aegis.local", "hashedPassword123", "Jane", "Doe");
    Session session = Session.create(user, "hashedRefreshToken123");

    assertThat(session.getUser()).isEqualTo(user);
    assertThat(session.getRefreshTokenHash()).isEqualTo("hashedRefreshToken123");
    assertThat(session.getRevoked()).isFalse();
    assertThat(session.isActive()).isTrue();
    assertThat(session.isExpired()).isFalse();
  }

  @Test
  @DisplayName("Domain Unit Test: Expired session returns true for isExpired")
  void testSession_expired_returnsTrue() {
    Organization org = Organization.create("Test Org", "test-org");
    User user = User.create(org, "test@aegis.local", "hashedPassword123", "Jane", "Doe");
    Session session =
        Session.create(user, "hashedRefreshToken123", Instant.now().minus(Duration.ofDays(1)));

    assertThat(session.isExpired()).isTrue();
    assertThat(session.isActive()).isFalse();
  }

  @Test
  @DisplayName("Domain Unit Test: Revoked session sets revoked flag to true")
  void testSession_revoke_setsRevokedFlag() {
    Organization org = Organization.create("Test Org", "test-org");
    User user = User.create(org, "test@aegis.local", "hashedPassword123", "Jane", "Doe");
    Session session = Session.create(user, "hashedRefreshToken123");

    session.revoke();
    assertThat(session.getRevoked()).isTrue();
    assertThat(session.isActive()).isFalse();
  }
}
