package net.likelion.bebc25.projectpatory.security.handler;

import jakarta.servlet.ServletException;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.security.jwt.JwtProvider;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
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

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private Authentication authentication;

    @Test
    @DisplayName("구글 로그인 성공 시 액세스 토큰을 쿼리로 담아 콜백 페이지로 리다이렉트한다")
    void onAuthenticationSuccess_redirectsWithAccessToken() throws IOException, ServletException {
        Member member = Member.builder()
                .id(10L)
                .email("user@company.com")
                .role("ROLE_USER")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(member, Map.of("sub", "google-sub-1"));
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(jwtProvider.createAccessToken(10L, "user@company.com", "ROLE_USER"))
                .willReturn("test-access-token");

        OAuth2SuccessHandler handler = new OAuth2SuccessHandler(jwtProvider);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("/oauth/callback.html?accessToken=test-access-token");
    }
}
