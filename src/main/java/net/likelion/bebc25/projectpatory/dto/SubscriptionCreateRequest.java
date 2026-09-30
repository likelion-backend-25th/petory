package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "구독 플랜 생성 요청")
public class SubscriptionCreateRequest {
    @Schema(description = "플랜 소유 회원 ID (로그인 회원 ID, 경로의 memberId와 동일해야 함)", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Long memberId;

    @Schema(description = "플랜 이름", example = "베이직", requiredMode = Schema.RequiredMode.REQUIRED)
    String planName;

    @Schema(description = "월 구독 가격 (원)", example = "4900", requiredMode = Schema.RequiredMode.REQUIRED)
    int price;

    @Schema(description = "플랜 설명", example = "월간 전용 피드 + 감사 메시지", requiredMode = Schema.RequiredMode.REQUIRED)
    String description;
}
