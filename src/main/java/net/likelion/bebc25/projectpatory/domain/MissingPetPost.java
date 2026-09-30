package net.likelion.bebc25.projectpatory.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "실종 동물 신고 게시글 도메인")
public class MissingPetPost {

    @Schema(description = "실종 신고 게시글 ID", example = "1")
    private Long id;

    @Schema(description = "작성자 회원 ID", example = "10")
    private Long memberId;

    @Schema(description = "실종 날짜", example = "2026-09-30")
    private LocalDate missingDate;

    @Schema(description = "실종 장소 주소", example = "서울특별시 강남구 테헤란로 123")
    private String missingAddress;

    @Schema(description = "실종 동물 상세 설명", example = "갈색 푸들, 빨간 목줄을 착용하고 있습니다.")
    private String detail;

    @Schema(description = "실종 동물 이미지 URL", example = "https://example.com/images/missing-pet.jpg")
    private String imageUrl;

    @Schema(description = "실종 신고 상태", example = "MISSING", allowableValues = {"MISSING", "FOUND", "CANCELLED"})
    private MissingPetStatus status;

    @Schema(description = "실종 장소 위도", example = "37.500123")
    private BigDecimal latitude;

    @Schema(description = "실종 장소 경도", example = "127.036456")
    private BigDecimal longitude;

    @Schema(description = "실종 신고 등록 일시", example = "2026-09-30T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "실종 신고 수정 일시", example = "2026-09-30T11:00:00")
    private LocalDateTime updatedAt;
}
