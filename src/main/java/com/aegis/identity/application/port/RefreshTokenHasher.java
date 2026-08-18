package com.aegis.identity.application.port;

public interface RefreshTokenHasher {

  String hash(String refreshToken);

  boolean matches(String refreshToken, String hash);
}
