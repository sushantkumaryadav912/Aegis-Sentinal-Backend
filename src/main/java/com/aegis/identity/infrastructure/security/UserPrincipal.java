package com.aegis.identity.infrastructure.security;

import com.aegis.identity.domain.entity.User;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails {

  private final UUID id;
  private final UUID organizationId;
  private final String email;
  private final String passwordHash;
  private final boolean active;
  private final Set<String> roles;
  private final Collection<? extends GrantedAuthority> authorities;

  public static UserPrincipal create(
      User user, Set<String> roles, Collection<? extends GrantedAuthority> authorities) {
    return UserPrincipal.builder()
        .id(user.getId())
        .organizationId(user.getOrganization().getId())
        .email(user.getEmail())
        .passwordHash(user.getPasswordHash())
        .active(user.getIsActive())
        .roles(roles)
        .authorities(authorities)
        .build();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return active;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }
}
