package com.aegis.identity.unit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserIdentityEntityTest {

  @Test
  @DisplayName("Domain Unit Test: UserIdentity constructor assigns fields correctly")
  void testUserIdentity_constructor_assignsFields() {
    Organization org = Organization.create("Test Org", "test-org");
    User user = User.create(org, "test@aegis.local", "hashedPassword123", "Jane", "Doe");
    UserIdentity identity =
        new UserIdentity(user, IdentityProvider.GOOGLE, "google-sub-12345", "jane.doe@gmail.com");

    assertThat(identity.getUser()).isEqualTo(user);
    assertThat(identity.getProvider()).isEqualTo(IdentityProvider.GOOGLE);
    assertThat(identity.getProviderSubject()).isEqualTo("google-sub-12345");
    assertThat(identity.getProviderEmail()).isEqualTo("jane.doe@gmail.com");
  }
}
