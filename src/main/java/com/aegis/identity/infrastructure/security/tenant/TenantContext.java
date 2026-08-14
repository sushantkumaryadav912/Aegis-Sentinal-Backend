package com.aegis.identity.infrastructure.security.tenant;

import java.util.UUID;

public record TenantContext(UUID userId, UUID organizationId, UUID workspaceId) {

  private static final ThreadLocal<TenantContext> CURRENT_TENANT = new ThreadLocal<>();

  public static void set(TenantContext context) {
    CURRENT_TENANT.set(context);
  }

  public static TenantContext get() {
    return CURRENT_TENANT.get();
  }

  public static void clear() {
    CURRENT_TENANT.remove();
  }
}
