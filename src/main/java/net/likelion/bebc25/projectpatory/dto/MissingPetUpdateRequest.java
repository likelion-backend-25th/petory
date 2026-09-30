package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "실종 동물 신고 수정 요청")
public record MissingPetUpdateRequest(
        @Schema(description = "수정할 실종 날짜", example = "2026-09-30")
        LocalDate missingDate,

        @Schema(description = "수정할 실종 장소 주소", example = "서울특별시 강남구 테헤란로 123")
        String missingAddress,

        @Schema(description = "수정할 실종 동물 상세 설명", example = "갈색 푸들, 빨간 목줄을 착용하고 있습니다.")
        String detail,

        @Schema(description = "수정할 실종 동물 이미지 URL", example = "https://example.com/images/missing-pet.jpg")
        String imageUrl,

        @Schema(description = "수정할 실종 장소 위도", example = "37.500123")
        BigDecimal latitude,

        @Schema(description = "수정할 실종 장소 경도", example = "127.036456")
        BigDecimal longitude
) {
}
