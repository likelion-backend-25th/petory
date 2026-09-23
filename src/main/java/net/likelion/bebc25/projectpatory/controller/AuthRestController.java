package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.ApiErrorResponse;
import net.likelion.bebc25.projectpatory.dto.LoginRequest;
import net.likelion.bebc25.projectpatory.dto.RefreshTokenRequest;
import net.likelion.bebc25.projectpatory.dto.TokenResponse;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.security.jwt.JwtProvider;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.security.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

@Tag(name = "Auth API", description = "회원 인증을 담당하는 REST 컨트롤러")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final MemberMapper memberMapper;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping("/login")
    @Operation(summary = "회원 로그인 시도", description = "주어진 email로 해당하는 사용자가 있는지 확인한 뒤 password가 일치하면 access/refresh 토큰을 반환")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "잘못된 이메일/비밀번호",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        // 1. 클라이언트가 입력한 이메일과 비밀번호로 미인증 토큰 생성
        Authentication unauthenticatedToken =
                new UsernamePasswordAuthenticationToken(request.email(), request.password());

        // 2. AuthenticationManager를 통한 인증 검증 위임
        Authentication authentication = authenticationManager.authenticate(unauthenticatedToken);

        // 3. 인증된 Principal로부터 회원 상세 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long memberId = userDetails.getMember().getId();
        String email = userDetails.getUsername();
        String role = userDetails.getMember().getRole();

        // 4. JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(memberId, email, role);
        String refreshToken = jwtProvider.createRefreshToken(memberId);

        // 5. 발급된 토큰 응답 반환 (Access Token 유효기간 1시간 = 3600초)
        TokenResponse response = TokenResponse.of(accessToken, refreshToken, 3600L);
        return ResponseEntity.ok(response);
    }

    // Refresh Token 기반 Access Token 갱신 엔드포인트
    @PostMapping("/refresh")
    @Operation(
            summary = "토큰 갱신",
            description = "유효한 refreshToken으로 새 access/refresh 토큰을 발급한다 (RTR)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "refreshToken 누락 또는 공백",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않거나 만료된 Refresh Token",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "토큰에 해당하는 회원이 존재하지 않음",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        // 1. Refresh Token 서명 및 만료 유효성 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("유효하지 않거나 만료된 Refresh Token입니다.");
        }

        // 2. 토큰 페이로드에서 회원 PK 추출
        Long memberId = jwtProvider.getMemberId(refreshToken);

        // 3. 데이터베이스 회원 존재 여부 및 최신 정보 조회
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(memberId);

        Member member = memberMapper.findById(memberId);
        if (member == null) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }

        // 4. 새 Access Token 및 Refresh Token 발급 (RTR 전략 적용)
        String newAccessToken = jwtProvider.createAccessToken(member.getId(), member.getEmail(), member.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(member.getId());

        TokenResponse response = TokenResponse.of(newAccessToken, newRefreshToken, 3600L);
        return ResponseEntity.ok(response);
    }
}