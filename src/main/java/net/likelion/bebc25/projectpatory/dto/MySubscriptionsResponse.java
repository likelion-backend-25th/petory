package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "내 구독 조회 응답")
public record MySubscriptionsResponse(
        @Schema(description = "구독 ID", example = "1")
        Long id,

        @Schema(description = "구독자 회원 ID", example = "4")
        Long memberId,

        @Schema(description = "구독 대상 회원 닉네임", example = "코코")
        String targetMember,

        @Schema(description = "플랜 이름", example = "베이직")
        String planName,

        @Schema(description = "구독 시작일", example = "2026-10-01")
        LocalDate startedAt,

        @Schema(description = "다음 결제일", example = "2026-11-01")
        LocalDate nextBillingAt,

        @Schema(description = "다음 달 구독 유지 동의 여부", example = "true")
        boolean agreement
) {
}
