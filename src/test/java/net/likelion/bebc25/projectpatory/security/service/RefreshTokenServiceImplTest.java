package net.likelion.bebc25.projectpatory.security.service;

import net.likelion.bebc25.projectpatory.domain.RefreshToken;
import net.likelion.bebc25.projectpatory.mapper.RefreshTokenMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    private static final long REFRESH_TOKEN_EXPIRATION_MILLIS = 604800000L;

    @Mock
    private RefreshTokenMapper refreshTokenMapper;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenServiceImpl(refreshTokenMapper, REFRESH_TOKEN_EXPIRATION_MILLIS);
    }

    @Test
    @DisplayName("refresh 토큰 저장 시 매퍼에 회원 ID, 토큰, 만료 시각을 전달한다")
    void saveRefreshToken_callsMapper() {
        // when
        refreshTokenService.saveRefreshToken(1L, "refresh-token-phone");

        // then
        ArgumentCaptor<LocalDateTime> expirationCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(refreshTokenMapper).saveRefreshToken(eq(1L), eq("refresh-token-phone"), expirationCaptor.capture());
        assertThat(expirationCaptor.getValue()).isCloseTo(expectedExpiration(), within(2, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("refresh 토큰 값으로 조회하면 매퍼 결과를 반환한다")
    void getRefreshToken_returnsMapperResult() {
        // given
        RefreshToken saved = RefreshToken.builder()
                .id(10L)
                .memberId(1L)
                .refreshToken("refresh-token-phone")
                .expiration(LocalDateTime.of(2026, 10, 5, 15, 0, 0))
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
    @DisplayName("refresh 토큰 갱신 시 매퍼에 id와 새 토큰, 만료 시각을 전달한다")
    void updateRefreshToken_callsMapper() {
        // when
        refreshTokenService.updateRefreshToken(10L, 1L, "refresh-token-phone-new");

        // then
        ArgumentCaptor<LocalDateTime> expirationCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(refreshTokenMapper).updateRefreshToken(
                eq(10L), eq(1L), eq("refresh-token-phone-new"), expirationCaptor.capture());
        assertThat(expirationCaptor.getValue()).isCloseTo(expectedExpiration(), within(2, ChronoUnit.SECONDS));
    }

    private LocalDateTime expectedExpiration() {
        return LocalDateTime.now().plus(Duration.ofMillis(REFRESH_TOKEN_EXPIRATION_MILLIS));
    }
}
