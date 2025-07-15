package gift.token.service;

import gift.exception.InvalidCredentialsException;
import gift.member.entity.Member;
import gift.token.entity.RefreshToken;
import gift.token.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    private static final Duration ACCESS_TOKEN_TTL = Duration.ofMinutes(15);
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenProvider(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(Member member) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ACCESS_TOKEN_TTL.toMillis());
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(member.getUuid().toString())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    @Transactional
    public String generateRefreshToken(Member member) {
        RefreshToken refreshToken = new RefreshToken(member.getUuid());
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public UUID getMemberUuidFromRefreshToken(String refreshTokenString)
            throws InvalidCredentialsException {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));
        if (refreshToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("Refresh token has expired");
        }

        return refreshToken.getMemberUuid();
    }

    public UUID getMemberUuidFromAccessToken(String token) throws InvalidCredentialsException {
        return UUID.fromString(getClaimsFromToken(token).getSubject());
    }

    private Claims getClaimsFromToken(String accessToken) throws InvalidCredentialsException {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new InvalidCredentialsException("Expired access token");
        } catch (MalformedJwtException e){
            throw new InvalidCredentialsException("Malformed access token");
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
