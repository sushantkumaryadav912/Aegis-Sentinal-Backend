package com.aegis.identity.infrastructure.security.jwt;

import com.aegis.identity.application.port.RefreshTokenHasher;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class Sha256RefreshTokenHasher implements RefreshTokenHasher {

  @Override
  public String hash(String refreshToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
    }
  }

  @Override
  public boolean matches(String refreshToken, String hash) {
    return MessageDigest.isEqual(
        hash(refreshToken).getBytes(StandardCharsets.UTF_8), hash.getBytes(StandardCharsets.UTF_8));
  }
}
