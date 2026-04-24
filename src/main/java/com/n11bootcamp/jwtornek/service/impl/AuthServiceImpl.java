package com.n11bootcamp.jwtornek.service.impl;

import com.n11bootcamp.jwtornek.auth.TokenManager;
import com.n11bootcamp.jwtornek.auth.TokenType;
import com.n11bootcamp.jwtornek.entity.RefreshToken;
import com.n11bootcamp.jwtornek.exception.InvalidTokenException;
import com.n11bootcamp.jwtornek.exception.RefreshTokenException;
import com.n11bootcamp.jwtornek.repository.RefreshTokenRepository;
import com.n11bootcamp.jwtornek.request.LoginRequest;
import com.n11bootcamp.jwtornek.request.LogoutRequest;
import com.n11bootcamp.jwtornek.request.RefreshTokenRequest;
import com.n11bootcamp.jwtornek.response.AuthResponse;
import com.n11bootcamp.jwtornek.service.AuthService;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenManager tokenManager;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           TokenManager tokenManager,
                           RefreshTokenRepository refreshTokenRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenManager = tokenManager;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        revokeActiveTokens(loginRequest.getUsername());
        return createAuthResponse(loginRequest.getUsername());
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshTokenValue = refreshTokenRequest.getRefreshToken();
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new InvalidTokenException("Refresh token zorunludur.");
        }

        if (!tokenManager.tokenValidate(refreshTokenValue, TokenType.REFRESH)) {
            throw new InvalidTokenException("Refresh token gecersiz veya suresi dolmus.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RefreshTokenException("Refresh token veritabaninda bulunamadi."));

        if (refreshToken.isRevoked()) {
            throw new RefreshTokenException("Refresh token daha once iptal edilmis.");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new RefreshTokenException("Refresh token suresi dolmus.");
        }

        String usernameFromToken = tokenManager.getUsernameToken(refreshTokenValue);
        if (!refreshToken.getUsername().equals(usernameFromToken)) {
            throw new RefreshTokenException("Refresh token kullanicisi dogrulanamadi.");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return createAuthResponse(refreshToken.getUsername());
    }

    @Override
    @Transactional
    public void logout(LogoutRequest logoutRequest) {
        String refreshTokenValue = logoutRequest.getRefreshToken();
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
            throw new InvalidTokenException("Logout icin refresh token gonderilmelidir.");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RefreshTokenException("Cikis yapilacak refresh token bulunamadi."));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    private AuthResponse createAuthResponse(String username) {
        String accessToken = tokenManager.generateAccessToken(username);
        String refreshTokenValue = tokenManager.generateRefreshToken(username);
        Date accessExpiration = tokenManager.getExpirationDate(accessToken);
        Date refreshExpiration = tokenManager.getExpirationDate(refreshTokenValue);

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenValue,
                username,
                refreshExpiration.toInstant(),
                false
        );
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                "Bearer",
                accessToken,
                accessExpiration.toInstant(),
                refreshTokenValue,
                refreshExpiration.toInstant()
        );
    }

    private void revokeActiveTokens(String username) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findAllByUsernameAndRevokedFalse(username);
        for (RefreshToken token : activeTokens) {
            token.setRevoked(true);
        }
        refreshTokenRepository.saveAll(activeTokens);
    }
}
