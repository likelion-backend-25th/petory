package net.likelion.bebc25.projectpatory.security.service;

import net.likelion.bebc25.projectpatory.domain.RefreshToken;
import net.likelion.bebc25.projectpatory.mapper.RefreshTokenMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    @DisplayName("refresh 토큰 저장 시 매퍼에 회원 ID, 토큰, 만료 시각을 전달한다")
    void saveRefreshToken_callsMapper() {
        // given
        LocalDateTime expiration = LocalDateTime.of(2026, 10, 5, 15, 0, 0);

        // when
        refreshTokenService.saveRefreshToken(1L, "refresh-token-phone", expiration);

        // then
        verify(refreshTokenMapper).saveRefreshToken(1L, "refresh-token-phone", expiration);
    }

    @Test
    @DisplayName("refresh 토큰 값으로 조회하면 매퍼 결과를 반환한다")
    void getRefreshToken_returnsMapperResult() {
        // given
        LocalDateTime expiration = LocalDateTime.of(2026, 10, 5, 15, 0, 0);
        RefreshToken saved = RefreshToken.builder()
                .id(10L)
                .memberId(1L)
                .refreshToken("refresh-token-phone")
                .expiration(expiration)
                .build();
        given(refreshTokenMapper.getRefreshToken("refresh-token-phone")).willReturn(saved);

        // when
        RefreshToken result = refreshTokenService.getRefreshToken("refresh-token-phone");

        // then
        assertThat(result).isEqualTo(saved);
        verify(refreshTokenMapper).getRefreshToken("refresh-token-phone");
    }

    @Test
    @DisplayName("저장되지 않은 refresh 토큰을 조회하면 null을 반환한다")
    void getRefreshToken_whenNotExists_returnsNull() {
        // given
        given(refreshTokenMapper.getRefreshToken("unknown-refresh-token")).willReturn(null);

        // when
        RefreshToken result = refreshTokenService.getRefreshToken("unknown-refresh-token");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("refresh 토큰 갱신 시 매퍼에 id와 새 토큰 정보를 전달한다")
    void updateRefreshToken_callsMapper() {
        // given
        LocalDateTime expiration = LocalDateTime.of(2026, 10, 12, 15, 0, 0);

        // when
        refreshTokenService.updateRefreshToken(10L, 1L, "refresh-token-phone-new", expiration);

        // then
        verify(refreshTokenMapper).updateRefreshToken(10L, 1L, "refresh-token-phone-new", expiration);
    }
}
