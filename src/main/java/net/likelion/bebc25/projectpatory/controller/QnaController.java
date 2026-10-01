package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.PostService;
import net.likelion.bebc25.projectpatory.service.QnaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    // qna 게시판 목록 조회
    @GetMapping
    public ResponseEntity<SliceResponse<PostListResponse>> getQnaList(
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size
    ) {
        SliceResponse<PostListResponse> response = qnaService.getQnaListCursor(lastPostId, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "해시태그 검색", description = "해시태그가 정확히 일치하는 게시글을 최신순으로 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<SliceResponse<PostListResponse>> searchQnaByHashtag(
            @Parameter(description = "검색할 해시태그 ('#' 제외)", example = "강아지")
            @RequestParam String hashtag,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size
    ) {
        SliceResponse<PostListResponse> response = qnaService.searchQnaByHashtag(hashtag, lastPostId, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Qna 상세 조회", description = "게시글 ID(postId)를 받아서 단건 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getQnaDetail(
            @Parameter(description = "조회할 게시글 ID", example = "3")
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails == null ? null : userDetails.getId();
        PostDetailResponse response = qnaService.getQnaDetail(postId, memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 등록합니다.")
    @PostMapping
    public ResponseEntity<PostCreateResponse> createQna(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 변경: 작성자 ID를 요청 Body 대신 JWT 인증 사용자에서 가져옴
            @RequestBody PostCreateRequest request
    ) {
        request.setMemberId(userDetails.getId());
        PostCreateResponse response = qnaService.createQna(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updateQna(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails, // 변경: @RequestParam memberId -> JWT 인증 사용자
            @RequestBody PostUpdateRequest request
    ) {
        qnaService.updateQna(postId, userDetails.getId(), request);
        return ResponseEntity.ok().build();
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deleteQna(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails // 변경: @RequestParam memberId -> JWT 인증 사용자
    ) {
        qnaService.deleteQna(postId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}