package com.aegis.identity.application.query;

import com.aegis.identity.application.port.OAuthUserInfo;

public record OAuthAuthenticationResult(
    Status status, AuthenticationResult authResult, OAuthUserInfo userInfo, String message) {
  public enum Status {
    SUCCESS,
    ONBOARDING_REQUIRED,
    LINKING_REQUIRED
  }

  public static OAuthAuthenticationResult success(AuthenticationResult authResult) {
    return new OAuthAuthenticationResult(
        Status.SUCCESS, authResult, null, "Authenticated successfully");
  }

  public static OAuthAuthenticationResult linkingRequired(OAuthUserInfo userInfo) {
    return new OAuthAuthenticationResult(
        Status.LINKING_REQUIRED, null, userInfo, "Account linking required with existing password");
  }

  public static OAuthAuthenticationResult onboardingRequired(OAuthUserInfo userInfo) {
    return new OAuthAuthenticationResult(
        Status.ONBOARDING_REQUIRED, null, userInfo, "Tenant onboarding required");
  }
}
