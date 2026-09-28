package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 등록합니다.")
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @Parameter(description = "대상 게시글 ID", example = "1")
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // 게시글 id, 로그인 회원 id, 댓글 내용을 response로 전달
        CommentResponse response = commentService.createComment(postId, userDetails.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
