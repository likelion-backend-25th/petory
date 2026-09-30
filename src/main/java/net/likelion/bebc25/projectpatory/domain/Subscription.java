package net.likelion.bebc25.projectpatory.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "구독 플랜")
public class Subscription {
    @Schema(description = "구독 플랜 ID", example = "1")
    Long id;

    @Schema(description = "플랜 소유 회원 ID", example = "2")
    Long memberId;

    @Schema(description = "플랜 이름", example = "베이직")
    String planName;

    @Schema(description = "월 구독 가격 (원)", example = "4900")
    int price;

    @Schema(description = "플랜 설명", example = "월간 전용 피드 + 감사 메시지")
    String description;

    @Schema(description = "플랜 상태", example = "ACTIVE")
    String status;
}
