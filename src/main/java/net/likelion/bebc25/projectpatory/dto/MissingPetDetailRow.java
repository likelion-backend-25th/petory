package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "실종 동물 상세 조회 DB 조회 행")
public record MissingPetDetailRow(
        @Schema(description = "실종 신고 게시글 ID", example = "1")
        Long id,

        @Schema(description = "작성자 회원 ID", example = "10")
        Long authorId,
        @Schema(description = "작성자 닉네임", example = "초코")
        String authorNickname,
        @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/images/profile.jpg")
        String authorProfileImage,

        @Schema(description = "실종 날짜", example = "2026-09-30")
        LocalDate missingDate,
        @Schema(description = "실종 장소 주소", example = "서울특별시 강남구 테헤란로 123")
        String missingAddress,
        @Schema(description = "실종 동물 상세 설명", example = "갈색 푸들, 빨간 목줄을 착용하고 있습니다.")
        String detail,
        @Schema(description = "실종 동물 이미지 URL", example = "https://example.com/images/missing-pet.jpg")
        String imageUrl,
        @Schema(description = "실종 신고 상태", example = "MISSING", allowableValues = {"MISSING", "FOUND", "CANCELLED"})
        String status,

        @Schema(description = "실종 장소 위도", example = "37.500123")
        BigDecimal latitude,
        @Schema(description = "실종 장소 경도", example = "127.036456")
        BigDecimal longitude,

        @Schema(description = "실종 신고 등록 일시", example = "2026-09-30T10:00:00")
        LocalDateTime createdAt,
        @Schema(description = "실종 신고 수정 일시", example = "2026-09-30T11:00:00")
        LocalDateTime updatedAt
) {
}
