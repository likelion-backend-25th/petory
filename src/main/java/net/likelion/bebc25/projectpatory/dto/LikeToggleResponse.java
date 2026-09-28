package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class LikeToggleResponse {

    @Schema(description = "좋아요 여부")
    private boolean liked;

    @Schema(description = "좋아요 개수")
    private Integer likeCount;

}
