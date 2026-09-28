package net.likelion.bebc25.projectpatory.security.service;


import net.likelion.bebc25.projectpatory.domain.RefreshToken;

import java.time.LocalDateTime;

public interface RefreshTokenService {
    void saveRefreshToken(Long memberId, String refreshToken, LocalDateTime expiration);

    RefreshToken getRefreshToken(String refreshToken);

    void updateRefreshToken(Long id, Long memberId, String refreshToken, LocalDateTime expiration);
}
