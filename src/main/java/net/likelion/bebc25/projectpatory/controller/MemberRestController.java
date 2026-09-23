package net.likelion.bebc25.projectpatory.controller;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.MemberProfileResponse;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MemberRestController {
    @PostMapping("signup")
    public void signUp() {
        
    }

    @GetMapping("/profile/{memberId}")
    public ResponseEntity<MemberProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // CustomUserDetails로부터 도메인 엔티티를 획득하여 마이페이지 응답 DTO로 변환
        Member member = userDetails.getMember();
        return ResponseEntity.ok(MemberProfileResponse.from(member));
    }

    @GetMapping("/profile/{memberId}/auth-info")
    public ResponseEntity<Map<String, Object>> getAuthInfo(Authentication authentication) {
        // 1. 사용자 식별자 및 보유 권한 획득(UserDetails 에서도 확인 가능)
        String email = authentication.getName();
        String roles = authentication.getAuthorities().toString();

        // 2. Authentication에만 존재하는 보안 부가 메타데이터(접속 IP 등) 추출
        String clientIp = "UNKNOWN";
        if (authentication.getDetails() instanceof WebAuthenticationDetails details) {
            clientIp = details.getRemoteAddress(); // 클라이언트 실제 접속 IP 주소
        }

        Map<String, Object> authInfo = Map.of(
                "email", email,
                "roles", roles,
                "clientIp", clientIp,
                "isAuthenticated", authentication.isAuthenticated()
        );

        return ResponseEntity.ok(authInfo);
    }
}