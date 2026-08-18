package com.aegis.identity.application.query;

import com.aegis.identity.domain.entity.Organization;
import com.aegis.identity.domain.entity.User;
import com.aegis.identity.domain.entity.Workspace;
import java.util.List;
import java.util.Set;

public record UserContext(
    User user,
    Organization organization,
    Workspace workspace,
    List<String> roles,
    Set<String> permissions) {}
