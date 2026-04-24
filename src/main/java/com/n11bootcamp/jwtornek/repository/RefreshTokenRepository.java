package com.n11bootcamp.jwtornek.repository;

import com.n11bootcamp.jwtornek.entity.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUsernameAndRevokedFalse(String username);
}
