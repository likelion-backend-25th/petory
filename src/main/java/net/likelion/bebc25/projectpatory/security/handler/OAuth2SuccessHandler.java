package net.likelion.bebc25.projectpatory.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.security.jwt.JwtProvider;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.security.service.RefreshTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;


    public OAuth2SuccessHandler(JwtProvider jwtProvider, RefreshTokenService refreshTokenService) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        // 1. CustomOAuth2UserService에서 반환한 통합 인증 객체 및 Member 엔티티 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Member member = userDetails.getMember();

        // 2. 백엔드 서비스 전용 자체 JWT 액세스 토큰 생성
        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());
        refreshTokenService.saveRefreshToken(member.getId(), refreshToken);

        // 3. 정적 콜백 페이지 URI 구성 (쿼리 파라미터로 액세스/리프레시 토큰 전달)
        String targetUrl = UriComponentsBuilder.fromPath("https://petory.likelion.shop")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();


        // 4. 콜백 URL로 브라우저 302 리다이렉트 실행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
