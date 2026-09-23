package net.likelion.bebc25.projectpatory.controller;

import net.likelion.bebc25.projectpatory.dto.PostListResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import net.likelion.bebc25.projectpatory.service.PostService;
import lombok.RequiredArgsConstructor;
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
}