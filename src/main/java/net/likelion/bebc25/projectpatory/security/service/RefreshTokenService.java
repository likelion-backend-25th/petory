package net.likelion.bebc25.projectpatory.security.service;


import net.likelion.bebc25.projectpatory.domain.RefreshToken;

public interface RefreshTokenService {
    void saveRefreshToken(Long memberId, String refreshToken);

    RefreshToken getRefreshToken(String refreshToken);

    void updateRefreshToken(Long id, Long memberId, String refreshToken);
}
