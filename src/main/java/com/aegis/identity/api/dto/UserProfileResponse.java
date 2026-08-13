package com.aegis.identity.api.dto;

import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private UUID id;
    private UUID organizationId;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean active;
    private Boolean mfaEnabled;
    private Set<String> roles;
    private Set<String> permissions;
}
