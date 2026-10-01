package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.BookmarkToggleResponse;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.PostBookmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/posts/{postId}/bookmarks")
@RequiredArgsConstructor
public class PostBookmarkController {

    private final PostBookmarkService postBookmarkService;

    @Operation(summary = "북마크 토글", description = "게시글 북마크를 등록하거나 취소합니다.")
    @PostMapping
    public ResponseEntity<BookmarkToggleResponse> toggleBookmark(
            @Parameter(description = "대상 게시글 ID", example = "3")
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BookmarkToggleResponse response = postBookmarkService.toggleBookmark(postId, userDetails.getId());
        return ResponseEntity.ok(response);
    }
}
