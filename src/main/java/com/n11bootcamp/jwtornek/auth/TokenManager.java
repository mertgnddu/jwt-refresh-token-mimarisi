package com.n11bootcamp.jwtornek.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenManager {

    private static final String TOKEN_TYPE_CLAIM = "type";

    private final Key key;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public TokenManager(@Value("${security.jwt.secret-key}") String secretKey,
                        @Value("${security.jwt.access-token-expiration-ms}") long accessTokenExpirationMs,
                        @Value("${security.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(String username) {
        return generateToken(username, TokenType.ACCESS, accessTokenExpirationMs);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, TokenType.REFRESH, refreshTokenExpirationMs);
    }

    public boolean tokenValidate(String token, TokenType expectedType) {
        try {
            Claims claims = getClaims(token);
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            return getUsernameToken(token) != null
                    && expectedType.name().equals(tokenType)
                    && isNotExpired(token);
        } catch (Exception exception) {
            return false;
        }
    }

    public String getUsernameToken(String token) {
        return getClaims(token).getSubject();
    }

    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    private String generateToken(String username, TokenType tokenType, long expirationMs) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuer("www.opendart.com")
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    private boolean isNotExpired(String token) {
        return getClaims(token).getExpiration().after(new Date(System.currentTimeMillis()));
    }

    private Claims getClaims(String token) {
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
        return claimsJws.getBody();
    }
}
