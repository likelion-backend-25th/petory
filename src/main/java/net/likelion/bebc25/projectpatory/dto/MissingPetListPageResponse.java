package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "실종 동물 목록 커서 페이지 응답")
public record MissingPetListPageResponse(
        @Schema(description = "전체 실종 신고 게시글 수", example = "42")
        Long totalCount,

        @Schema(description = "실종 동물 목록")
        List<MissingPetListResponse> items,

        @Schema(description = "다음 페이지 조회용 커서", example = "25", nullable = true)
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
