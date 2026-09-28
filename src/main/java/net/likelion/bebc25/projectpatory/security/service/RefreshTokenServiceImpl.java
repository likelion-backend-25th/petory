package net.likelion.bebc25.projectpatory.security.service;

import net.likelion.bebc25.projectpatory.domain.RefreshToken;
import net.likelion.bebc25.projectpatory.mapper.RefreshTokenMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenMapper refreshTokenMapper;
    private final long refreshTokenExpiration;


    public RefreshTokenServiceImpl(
            RefreshTokenMapper refreshTokenMapper,
            @Value("${jwt.refresh-token-expiration}") Long refreshTokenExpiration) {
        this.refreshTokenMapper = refreshTokenMapper;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    public void saveRefreshToken(Long memberId, String refreshToken) {
        LocalDateTime expiration = LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpiration));
        refreshTokenMapper.saveRefreshToken(memberId, refreshToken, expiration);
    }

    @Override
    public RefreshToken getRefreshToken(String refreshToken) {
        return refreshTokenMapper.getRefreshToken(refreshToken);
    }

    @Override
    public void updateRefreshToken(Long id, Long memberId, String refreshToken) {
        LocalDateTime expiration = LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpiration));
        refreshTokenMapper.updateRefreshToken(id, memberId, refreshToken, expiration);
    }
}
