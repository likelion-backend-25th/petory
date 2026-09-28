package net.likelion.bebc25.projectpatory.security.service;

import net.likelion.bebc25.projectpatory.domain.RefreshToken;
import net.likelion.bebc25.projectpatory.mapper.RefreshTokenMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenMapper refreshTokenMapper;

    public RefreshTokenServiceImpl(RefreshTokenMapper refreshTokenMapper) {
        this.refreshTokenMapper = refreshTokenMapper;
    }

    @Override
    public void saveRefreshToken(Long memberId, String refreshToken, LocalDateTime expiration) {
        refreshTokenMapper.saveRefreshToken(memberId, refreshToken, expiration);
    }

    @Override
    public RefreshToken getRefreshToken(String refreshToken) {
        return refreshTokenMapper.getRefreshToken(refreshToken);
    }

    @Override
    public void updateRefreshToken(Long id, Long memberId, String refreshToken, LocalDateTime expiration) {
        refreshTokenMapper.updateRefreshToken(id, memberId, refreshToken, expiration);
    }
}
