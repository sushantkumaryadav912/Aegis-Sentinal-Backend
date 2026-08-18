package com.aegis.identity.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserEntityTest {

  @Test
  @DisplayName("Domain Unit Test: User creation initializes active status and attributes correctly")
  void testUser_creation_setsDefaults() {
    Organization org = Organization.create("Test Org", "test-org");
    User user = User.create(org, "test@aegis.local", "hashedPassword123", "Jane", "Doe");

    assertThat(user.getEmail()).isEqualTo("test@aegis.local");
    assertThat(user.getFirstName()).isEqualTo("Jane");
    assertThat(user.getLastName()).isEqualTo("Doe");
    assertThat(user.getPasswordHash()).isEqualTo("hashedPassword123");
    assertThat(user.getOrganization()).isEqualTo(org);
    assertThat(user.isActive()).isTrue();
  }

  @Test
  @DisplayName("Domain Unit Test: Deactivating user updates active state")
  void testUser_deactivation_updatesState() {
    Organization org = Organization.create("Test Org", "test-org");
    User user = User.create(org, "test@aegis.local", "hashedPassword123", "Jane", "Doe");

    user.setIsActive(false);
    assertThat(user.isActive()).isFalse();
  }
}
