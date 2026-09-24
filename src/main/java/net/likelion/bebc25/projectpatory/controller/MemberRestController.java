package net.likelion.bebc25.projectpatory.controller;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.MemberProfileResponse;
import net.likelion.bebc25.projectpatory.dto.MyPagePostResponse;
import net.likelion.bebc25.projectpatory.dto.SignUpRequest;
import net.likelion.bebc25.projectpatory.dto.SignUpResponse;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.MemberService;
import net.likelion.bebc25.projectpatory.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MemberRestController {
    private final MemberService memberService;
    private final PostService postService;

    public MemberRestController(MemberService memberService, PostService postService) {
        this.memberService = memberService;
        this.postService = postService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        Member member = memberService.signup(request);
        return ResponseEntity.ok(SignUpResponse.from(member));
    }

    @GetMapping("/profile/{memberId}")
    public ResponseEntity<MemberProfileResponse> getMyProfile(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Member member = memberService.findMemberById(memberId);
        return ResponseEntity.ok(memberService.getMyProfile(member));
    }

    @GetMapping("/profile/{memberId}/posts")
    public ResponseEntity<List<MyPagePostResponse>> getMyMainPosts(
            @PathVariable Long memberId
    ) {return ResponseEntity.ok(postService.getMyMainPosts(memberId));}

    @GetMapping("/profile/{memberId}/qna")
    public ResponseEntity<List<MyPagePostResponse>> getMyQnAPosts(
            @PathVariable Long memberId
    ) {return ResponseEntity.ok(postService.getMyQnAPosts(memberId));}

    @GetMapping("/profile/{memberId}/bookmarks")
    public ResponseEntity<List<MyPagePostResponse>> getMyBookmarks(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {return ResponseEntity.ok(postService.getMyBookmarks(memberId, userDetails.getId()));}


    // 서비스용 기능x / 학습 or 디버깅용
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