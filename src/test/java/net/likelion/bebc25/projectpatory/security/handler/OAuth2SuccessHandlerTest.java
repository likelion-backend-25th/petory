package net.likelion.bebc25.projectpatory.security.handler;

import jakarta.servlet.ServletException;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.security.jwt.JwtProvider;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.security.service.RefreshTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    @Test
    @DisplayName("소셜 로그인 성공 시 리프레시 토큰을 저장하고 두 토큰을 콜백 쿼리로 전달한다")
    void onAuthenticationSuccess_savesRefreshTokenAndRedirects() throws IOException, ServletException {
        Member member = Member.builder()
                .id(10L)
                .email("user@company.com")
                .role("ROLE_USER")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member, Map.of("sub", "google-sub-1"));
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(jwtProvider.createAccessToken(10L, "user@company.com", "ROLE_USER"))
                .willReturn("test-access-token");
        given(jwtProvider.createRefreshToken(10L)).willReturn("test-refresh-token");

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(jwtProvider, refreshTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(refreshTokenService).saveRefreshToken(10L, "test-refresh-token");
        assertThat(response.getRedirectedUrl())
                .isEqualTo("/oauth/callback.html?accessToken=test-access-token&refreshToken=test-refresh-token");
    }
}
