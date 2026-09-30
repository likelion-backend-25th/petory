package net.likelion.bebc25.projectpatory.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "실종 신고 상태", allowableValues = {"MISSING", "FOUND", "CANCELLED"})
public enum MissingPetStatus {

    @Schema(description = "실종 중")
    MISSING,

    @Schema(description = "찾음")
    FOUND,

    @Schema(description = "신고 취소")
    CANCELLED
}
