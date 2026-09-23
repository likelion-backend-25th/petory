package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @PathVariable Long postId
    ) {
        PostDetailResponse response = postService.getPostDetail(postId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "게시글 작성", description = "새로운 게시글을 등록합니다.")
    @PostMapping
    public ResponseEntity<PostCreateResponse> createPost(@RequestBody PostCreateRequest request) {
        PostCreateResponse response = postService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}