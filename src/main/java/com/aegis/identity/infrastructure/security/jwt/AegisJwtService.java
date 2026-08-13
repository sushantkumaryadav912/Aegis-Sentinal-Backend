package com.aegis.identity.infrastructure.security.jwt;

import com.aegis.identity.application.port.TokenService;
import com.aegis.identity.domain.entity.User;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class AegisJwtService implements TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public AegisJwtService(
            JwtEncoder jwtEncoder,
            JwtProperties properties) {

        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    @Override
    public String generateAccessToken(User user) {

        Instant issuedAt = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .issuedAt(issuedAt)
                .expiresAt(
                        issuedAt.plus(properties.accessTokenTtl()))
                .claim("email", user.getEmail())
                .claim("token_type", "access")
                .build();

        return encode(claims);
    }

    @Override
    public String generateRefreshToken(User user) {

        Instant issuedAt = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getId().toString())
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiresAt(
                        issuedAt.plus(properties.refreshTokenTtl()))
                .claim("token_type", "refresh")
                .build();

        return encode(claims);
    }

    private String encode(JwtClaimsSet claims) {

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
