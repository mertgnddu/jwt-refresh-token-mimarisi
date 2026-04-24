package com.n11bootcamp.jwtornek.service;

import com.n11bootcamp.jwtornek.request.LoginRequest;
import com.n11bootcamp.jwtornek.request.LogoutRequest;
import com.n11bootcamp.jwtornek.request.RefreshTokenRequest;
import com.n11bootcamp.jwtornek.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

    void logout(LogoutRequest logoutRequest);
}
