package cat.itacademy.s04.t02.n02.drinkcardmoa.iam.infrastructure.adapter.out.security;

import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.application.port.out.TokenService;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.exception.InvalidTokenException;
import cat.itacademy.s04.t02.n02.drinkcardmoa.iam.domain.model.aggregate.User;
import cat.itacademy.s04.t02.n02.drinkcardmoa.shared.domain.VolunteerID;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtTokenServiceAdapter implements TokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenServiceAdapter.class);

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtTokenServiceAdapter(@Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes:1440}") long expirationMinutes) {

        if(secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalArgumentException("Secret key must be at least 32 bytes long");

        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant expiration = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(user.getId().asString())
                .claim("email", user.getEmail().asString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public VolunteerID extractVolunteerID(String token) {
        Claims claims = extractAllClaims(token);
        String uuidString = claims.getSubject();

        try {
            return VolunteerID.from(uuidString);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("Invalid VolunteerID format in token: " + uuidString);
        }
    }

    @Override
    public String extractEmail(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("email", String.class);
    }

    @Override
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (JwtException e) {
            throw new InvalidTokenException("Failed to parse JWT token: " + e.getMessage());
        }
    }
}
