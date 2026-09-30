package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * REQ-POST-01: 메인 피드 목록 조회 (커서 기반 무한 스크롤)
     *
     * @param lastPostId 직전에 요청받은 목록의 마지막 게시글 ID (처음 조회 시 null)
     * @param size       한 번에 요청할 게시글 개수 (기본값 10)
     *
     * 요청 예시: GET /api/v1/posts?lastPostId=25&size=10
     */
    @GetMapping
    public ResponseEntity<SliceResponse<PostListResponse>> getPostList(
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size
    ) {
        SliceResponse<PostListResponse> response = postService.getPostListCursor(lastPostId, size);
        return ResponseEntity.ok(response);
    }

    /**
     * 해시태그 검색 (커서 기반 무한 스크롤)
     *
     * @param hashtag    검색할 해시태그 ('#' 은 빼고 보내는 것을 권장, URL에 '#' 을 넣으려면 %23 으로 인코딩해야 함)
     * @param lastPostId 직전에 요청받은 목록의 마지막 게시글 ID (처음 조회 시 null)
     * @param size       한 번에 요청할 게시글 개수 (기본값 10)
     *
     * 요청 예시: GET /api/v1/posts/search?hashtag=강아지&size=10
     */
    @Operation(summary = "해시태그 검색", description = "해시태그가 정확히 일치하는 게시글을 최신순으로 조회합니다.")
    @GetMapping("/search")
    public ResponseEntity<SliceResponse<PostListResponse>> searchPostsByHashtag(
            @Parameter(description = "검색할 해시태그 ('#' 제외)", example = "강아지")
            @RequestParam String hashtag,
            @RequestParam(required = false) Long lastPostId,
            @RequestParam(defaultValue = "10") int size
    ) {
        SliceResponse<PostListResponse> response = postService.searchPostsByHashtag(hashtag, lastPostId, size);
        return ResponseEntity.ok(response);
    }

    /**
     * REQ-POST-02: 게시글 상세 조회 (단건 조회)
     *
     * @param postId 조회할 게시글 ID
     *
     * 요청 예시: GET /api/v1/posts/3
     */
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID(postId)를 받아서 단건 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(
            @Parameter(description = "조회할 게시글 ID", example = "3")
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long memberId = userDetails == null ? null : userDetails.getId();
        PostDetailResponse response = postService.getPostDetail(postId, memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 등록합니다.")
    @PostMapping
    public ResponseEntity<PostCreateResponse> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails, // 변경: 작성자 ID를 요청 Body 대신 JWT 인증 사용자에서 가져옴
            @RequestBody PostCreateRequest request
    ) {
        request.setMemberId(userDetails.getId());
        PostCreateResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails, // 변경: @RequestParam memberId -> JWT 인증 사용자
            @RequestBody PostUpdateRequest request
    ) {
        postService.updatePost(postId, userDetails.getId(), request);
        return ResponseEntity.ok().build();
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails // 변경: @RequestParam memberId -> JWT 인증 사용자
    ) {
        postService.deletePost(postId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}