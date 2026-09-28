package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.ApiErrorResponse;
import net.likelion.bebc25.projectpatory.dto.MemberProfileResponse;
import net.likelion.bebc25.projectpatory.dto.MyPagePostResponse;
import net.likelion.bebc25.projectpatory.dto.MyProfileResponse;
import net.likelion.bebc25.projectpatory.dto.ProfileResponse;
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

@Tag(name = "MyPage API", description = "마이페이지 및 회원 프로필 조회를 담당하는 REST 컨트롤러")
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
    @Operation(summary = "회원 가입", description = "신규 회원을 등록한다")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody SignUpRequest request) {
        Member member = memberService.signup(request);
        return ResponseEntity.ok(SignUpResponse.from(member));
    }

    @GetMapping("/profile/{memberId}")
    @Operation(
            summary = "프로필 조회",
            description = "회원 프로필을 조회한다. 본인 조회 시 이메일·주소 등 민감 정보가 포함된 MyProfileResponse를, "
                    + "타인 조회 시 민감 정보가 제외된 MemberProfileResponse를 반환한다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(oneOf = {MyProfileResponse.class, MemberProfileResponse.class}))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ProfileResponse> getMyProfile(
            @Parameter(description = "조회할 회원 ID", example = "1")
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long loginMemberId = userDetails.getId();
        Member member = memberService.findMemberById(memberId);
        return ResponseEntity.ok(memberService.getMyProfile(member, loginMemberId));
    }

    @GetMapping("/profile/{memberId}/posts")
    @Operation(
            summary = "마이페이지 메인 게시글 목록 조회",
            description = "해당 회원이 작성한 메인 피드 게시글 목록을 조회한다"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "메인 게시글 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MyPagePostResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<List<MyPagePostResponse>> getMyMainPosts(
            @Parameter(description = "조회할 회원 ID", example = "1")
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(postService.getMyMainPosts(memberId));
    }

    @GetMapping("/profile/{memberId}/qna")
    @Operation(
            summary = "마이페이지 QnA 게시글 목록 조회",
            description = "해당 회원이 작성한 QnA 게시글 목록을 조회한다"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "QnA 게시글 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MyPagePostResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<List<MyPagePostResponse>> getMyQnAPosts(
            @Parameter(description = "조회할 회원 ID", example = "1")
            @PathVariable Long memberId
    ) {
        return ResponseEntity.ok(postService.getMyQnAPosts(memberId));
    }

    @GetMapping("/profile/{memberId}/bookmarks")
    @Operation(
            summary = "마이페이지 북마크 목록 조회",
            description = "본인이 북마크한 게시글 목록을 조회한다. 타인의 북마크는 조회할 수 없다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "북마크 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MyPagePostResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "타인 북마크 조회 시도",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<List<MyPagePostResponse>> getMyBookmarks(
            @Parameter(description = "조회할 회원 ID (로그인 회원 ID와 동일해야 함)", example = "1")
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(postService.getMyBookmarks(memberId, userDetails.getId()));
    }

    // 서비스용 기능x / 학습 or 디버깅용
    @Hidden
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
