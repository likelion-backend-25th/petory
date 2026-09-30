package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "실종 동물 목록 항목 응답")
public record MissingPetListResponse(
        @Schema(description = "실종 신고 게시글 ID", example = "1")
        Long id,

        @Schema(description = "실종 동물 대표 이미지 URL", example = "https://example.com/images/missing-pet.jpg")
        String imageUrl
) {
}
