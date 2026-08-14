package com.aegis.identity.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.repository.OrganizationRepository;
import com.aegis.identity.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UserRepositorySliceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Test
    @DisplayName("Repository Test: findByEmail returns saved user entity")
    void testFindByEmail_returnsUser() {
        Organization org = organizationRepository.save(Organization.create("Repo Org " + UUID.randomUUID(), "repo-org-" + UUID.randomUUID()));
        String email = "repo-user-" + UUID.randomUUID() + "@aegis.test";
        User user = userRepository.save(User.create(org, email, "hash", "Repo", "User"));

        Optional<User> found = userRepository.findByEmail(email);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(user.getId());
        assertThat(found.get().getEmail()).isEqualTo(email);
    }

    @Test
    @DisplayName("Repository Test: existsByEmail returns true for existing email and false for unknown email")
    void testExistsByEmail() {
        Organization org = organizationRepository.save(Organization.create("Repo Org " + UUID.randomUUID(), "repo-org-" + UUID.randomUUID()));
        String email = "exists-" + UUID.randomUUID() + "@aegis.test";
        userRepository.save(User.create(org, email, "hash", "Exists", "User"));

        assertThat(userRepository.existsByEmail(email)).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent-" + UUID.randomUUID() + "@aegis.test")).isFalse();
    }
}
