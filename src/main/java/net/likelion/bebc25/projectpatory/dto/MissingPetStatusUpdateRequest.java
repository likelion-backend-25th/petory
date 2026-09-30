package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import net.likelion.bebc25.projectpatory.domain.MissingPetStatus;

@Schema(description = "실종 동물 상태 변경 요청")
public record MissingPetStatusUpdateRequest(
        @Schema(description = "변경할 실종 신고 상태", example = "FOUND", allowableValues = {"MISSING", "FOUND", "CANCELLED"}, requiredMode = Schema.RequiredMode.REQUIRED)
        MissingPetStatus status
) {
}
