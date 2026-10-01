package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "구독 첫 결제 요청")
public class SubscriptionRecordCreateRequest {
    @Schema(description = "구독 대상 회원 ID. 경로의 memberId, 플랜 소유 회원 ID와 같아야 한다.", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    Long targetMemberId;

    @Schema(description = "구독할 플랜 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long planId;

    @Schema(description = "포트원에서 발급받은 빌링키", example = "billing-key-1", requiredMode = Schema.RequiredMode.REQUIRED)
    String billingKey;
}
