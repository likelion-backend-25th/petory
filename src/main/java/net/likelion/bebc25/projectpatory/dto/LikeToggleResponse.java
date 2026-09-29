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
@Schema(description = "좋아요 토글 응답")
public class LikeToggleResponse {

    @Schema(description = "좋아요 여부", example = "true")
    private boolean liked;

    @Schema(description = "좋아요 개수", example = "4")
    private long likeCount;
}
