package com.aegis.identity.application.port;

import com.aegis.identity.domain.entity.User;
import java.util.UUID;

public interface TokenService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    UUID extractUserIdFromRefreshToken(String refreshToken);
}
