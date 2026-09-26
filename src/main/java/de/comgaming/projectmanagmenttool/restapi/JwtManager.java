package de.comgaming.projectmanagmenttool.restapi;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static io.jsonwebtoken.Jwts.*;

public class JwtManager {

    private static final long EXPIRATION = 1000L * 60 * 60 * 24;
    private final SecretKey key;

    public JwtManager() {
        String secret = new RestAPIConfig().getSecret();

        if (secret.length() < 32)
            throw new IllegalStateException(
                    "JWT Secret muss mindestens 32 Zeichen lang sein.");

        key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long accountId, String username) {
        Date now = new Date();

        return builder()
                .subject(String.valueOf(accountId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    public Long getAccountId(String token) {
        try {
            Claims claims = parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.parseLong(claims.getSubject());

        } catch (Exception e) {
            return null;
        }
    }
}
