package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "실종 동물 목격 제보 응답")
public record MissingPetReportResponse(
        @Schema(description = "목격 제보 ID", example = "1")
        Long id,

        @Schema(description = "목격 제보자 정보")
        MemberSummaryResponse reporter,

        @Schema(description = "목격 장소 주소", example = "서울특별시 강남구 역삼동 123-45")
        String address,

        @Schema(description = "목격 상세 내용", example = "비슷한 강아지를 공원 입구 근처에서 봤습니다.")
        String detail,

        @Schema(description = "목격 사진 이미지 URL", example = "https://example.com/images/report.jpg")
        String imageUrl,

        @Schema(description = "목격 일시", example = "2026-09-30T14:30:00")
        LocalDateTime sightAt,

        @Schema(description = "목격 장소 위도", example = "37.501234")
        BigDecimal latitude,

        @Schema(description = "목격 장소 경도", example = "127.037890")
        BigDecimal longitude,

        @Schema(description = "목격 제보 등록 일시", example = "2026-09-30T15:00:00")
        LocalDateTime createdAt
) {
}
