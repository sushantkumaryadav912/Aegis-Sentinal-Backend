package com.aegis.identity.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.aegis.identity.application.service.CreateSessionService;
import com.aegis.identity.application.service.OAuthAccountLinkService;
import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserIdentity;
import com.aegis.identity.domain.model.IdentityProvider;
import com.aegis.identity.domain.repository.UserIdentityRepository;
import com.aegis.identity.domain.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OAuthAccountLinkServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserIdentityRepository userIdentityRepository;

    @Mock
    private CreateSessionService createSessionService;

    private OAuthAccountLinkService service;
    private User testUser;

    @BeforeEach
    void setUp() {
        service = new OAuthAccountLinkService(userRepository, userIdentityRepository, null, createSessionService);
        Organization org = Organization.create("Test Org", "test-org");
        testUser = User.create(org, "user@aegis.test", "pwd", "Test", "User");
        testUser.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Service Unit Test: Authenticated account link succeeds when no duplicate identity exists")
    void testLinkAuthenticatedUser_success() {
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userIdentityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, "sub-1234"))
                .thenReturn(Optional.empty());
        when(userIdentityRepository.save(any(UserIdentity.class))).thenAnswer(i -> i.getArgument(0));

        UserIdentity result = service.linkAuthenticatedUser(userId, IdentityProvider.GOOGLE, "sub-1234", "user@gmail.com");

        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getProviderSubject()).isEqualTo("sub-1234");
        assertThat(result.getProviderEmail()).isEqualTo("user@gmail.com");
    }

    @Test
    @DisplayName("Service Unit Test: Linking identity owned by another user throws IllegalStateException")
    void testLinkAuthenticatedUser_ownedByAnotherUser_throws() {
        UUID userId = testUser.getId();
        Organization org = Organization.create("Test Org", "test-org");
        User otherUser = User.create(org, "other@aegis.test", "pwd", "Other", "User");
        otherUser.setId(UUID.randomUUID());
        UserIdentity existingIdentity = new UserIdentity(otherUser, IdentityProvider.GOOGLE, "sub-1234", "other@gmail.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userIdentityRepository.findByProviderAndProviderSubject(IdentityProvider.GOOGLE, "sub-1234"))
                .thenReturn(Optional.of(existingIdentity));

        assertThatThrownBy(() -> service.linkAuthenticatedUser(userId, IdentityProvider.GOOGLE, "sub-1234", "user@gmail.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already linked to another account");
    }
}
