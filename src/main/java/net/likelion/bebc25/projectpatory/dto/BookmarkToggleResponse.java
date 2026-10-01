package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "북마크 토글 응답")
public class BookmarkToggleResponse {

    @Schema(description = "대상 게시글 ID", example = "3")
    private Long postId;

    @Schema(description = "북마크 여부", example = "true")
    private boolean isBookmarked;
}
