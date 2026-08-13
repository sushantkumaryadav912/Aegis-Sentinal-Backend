package com.aegis.identity.application.port;

import com.aegis.identity.domain.entity.User;

public interface TokenService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);
}
