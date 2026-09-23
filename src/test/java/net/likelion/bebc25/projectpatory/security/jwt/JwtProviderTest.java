package net.likelion.bebc25.projectpatory.security.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
    private static final long ACCESS_TOKEN_EXPIRATION = 3_600_000L;
    private static final long REFRESH_TOKEN_EXPIRATION = 604_800_000L;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("Access Token을 생성하고 이메일·회원ID·역할을 추출한다")
    void createAccessToken_andExtractClaims() {
        // given
        Long memberId = 1L;
        String email = "test@petory.com";
        String role = "ROLE_USER";

        // when
        String accessToken = jwtProvider.createAccessToken(memberId, email, role);

        // then
        assertThat(accessToken).isNotBlank();
        assertThat(jwtProvider.validateToken(accessToken)).isTrue();
        assertThat(jwtProvider.getMemberId(accessToken)).isEqualTo(memberId);
        assertThat(jwtProvider.getEmail(accessToken)).isEqualTo(email);

        Claims claims = jwtProvider.parseClaims(accessToken);
        assertThat(claims.getSubject()).isEqualTo(String.valueOf(memberId));
        assertThat(claims.get("email", String.class)).isEqualTo(email);
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        assertThat(roles).contains(role);
    }

    @Test
    @DisplayName("Refresh Token을 생성하고 회원ID를 추출한다")
    void createRefreshToken_andExtractMemberId() {
        // given
        Long memberId = 10L;

        // when
        String refreshToken = jwtProvider.createRefreshToken(memberId);

        // then
        assertThat(refreshToken).isNotBlank();
        assertThat(jwtProvider.validateToken(refreshToken)).isTrue();
        assertThat(jwtProvider.getMemberId(refreshToken)).isEqualTo(memberId);
        assertThat(jwtProvider.getEmail(refreshToken)).isNull();
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 validateToken이 false를 반환한다")
    void validateToken_whenInvalid_returnsFalse() {
        // given
        String invalidToken = "invalid.jwt.token";

        // when & then
        assertThat(jwtProvider.validateToken(invalidToken)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 validateToken이 false를 반환한다")
    void validateToken_whenExpired_returnsFalse() throws InterruptedException {
        // given
        JwtProvider shortLivedProvider = new JwtProvider(SECRET, 1L, 1L);
        String expiredToken = shortLivedProvider.createAccessToken(1L, "test@petory.com", "ROLE_USER");
        Thread.sleep(10);

        // when & then
        assertThat(shortLivedProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰이라도 parseClaims로 클레임을 읽을 수 있다")
    void parseClaims_whenExpired_stillReturnsClaims() throws InterruptedException {
        // given
        JwtProvider shortLivedProvider = new JwtProvider(SECRET, 1L, 1L);
        String expiredToken = shortLivedProvider.createAccessToken(7L, "expired@petory.com", "ROLE_USER");
        Thread.sleep(10);

        // when
        Claims claims = shortLivedProvider.parseClaims(expiredToken);

        // then
        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.get("email", String.class)).isEqualTo("expired@petory.com");
    }
}
