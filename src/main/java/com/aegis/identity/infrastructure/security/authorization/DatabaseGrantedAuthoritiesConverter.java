package com.aegis.identity.infrastructure.security.authorization;

import com.aegis.identity.domain.entity.Permission;
import com.aegis.identity.domain.entity.Role;
import com.aegis.identity.domain.entity.UserRole;
import com.aegis.identity.domain.repository.UserRoleRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseGrantedAuthoritiesConverter
    implements Converter<Jwt, Collection<GrantedAuthority>> {

  private final UserRoleRepository userRoleRepository;

  public DatabaseGrantedAuthoritiesConverter(UserRoleRepository userRoleRepository) {
    this.userRoleRepository = userRoleRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Collection<GrantedAuthority> convert(Jwt jwt) {

    String tokenType = jwt.getClaimAsString("token_type");
    if (!"access".equals(tokenType)) {
      return Set.of();
    }

    UUID userId;

    try {
      userId = UUID.fromString(jwt.getSubject());
    } catch (IllegalArgumentException exception) {
      return Set.of();
    }

    Collection<UserRole> userRoles = userRoleRepository.findByUserId(userId);

    Set<GrantedAuthority> authorities = new HashSet<>();

    for (UserRole userRole : userRoles) {
      Role role = userRole.getRole();

      if (role != null) {
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

        if (role.getPermissions() != null) {
          for (Permission permission : role.getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(permission.getName()));
          }
        }
      }
    }

    return new ArrayList<>(authorities);
  }
}
