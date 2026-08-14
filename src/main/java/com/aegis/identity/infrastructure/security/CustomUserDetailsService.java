package com.aegis.identity.infrastructure.security;

import com.aegis.identity.domain.entity.Permission;
import com.aegis.identity.domain.entity.Role;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.UserRole;
import com.aegis.identity.domain.repository.UserRepository;
import com.aegis.identity.domain.repository.UserRoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + email));

    return buildUserPrincipal(user);
  }

  @Transactional(readOnly = true)
  public UserDetails loadUserById(UUID userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

    return buildUserPrincipal(user);
  }

  private UserPrincipal buildUserPrincipal(User user) {
    List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());

    Set<String> roles = new HashSet<>();
    Set<GrantedAuthority> authorities = new HashSet<>();

    for (UserRole userRole : userRoles) {
      Role role = userRole.getRole();
      roles.add(role.getName());
      authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

      for (Permission permission : role.getPermissions()) {
        authorities.add(new SimpleGrantedAuthority(permission.getName()));
      }
    }

    return UserPrincipal.create(user, roles, authorities);
  }
}
