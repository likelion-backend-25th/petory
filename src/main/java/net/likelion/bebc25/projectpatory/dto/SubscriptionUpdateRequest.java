package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "구독 플랜 수정 요청. 가격은 변경되지 않는다.")
public class SubscriptionUpdateRequest {
    @Schema(description = "수정할 구독 플랜 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long id;

    @Schema(description = "플랜 소유 회원 ID", example = "2")
    Long memberId;

    @Schema(description = "플랜 이름", example = "프리미엄", requiredMode = Schema.RequiredMode.REQUIRED)
    String planName;

    @Schema(description = "플랜 설명", example = "전용 피드 + 월 1회 화상 만남", requiredMode = Schema.RequiredMode.REQUIRED)
    String description;

    @Schema(description = "플랜 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "DELETED"})
    String status;
}
