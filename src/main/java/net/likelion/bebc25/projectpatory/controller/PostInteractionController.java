package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.LikeToggleResponse;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.PostLikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostInteractionController {

    private final PostLikeService postLikeService;

    @Operation(summary = "좋아요 토글", description = "게시글 좋아요를 등록하거나 취소합니다.")
    @PostMapping
    public ResponseEntity<LikeToggleResponse> toggleLike(
            @Parameter(description = "대상 게시글 ID", example = "1")
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 게시글 id, 로그인한 회원정보를 response로 전달
        LikeToggleResponse response = postLikeService.toggleLike(postId, userDetails.getId());
        return ResponseEntity.ok(response);
    }
}