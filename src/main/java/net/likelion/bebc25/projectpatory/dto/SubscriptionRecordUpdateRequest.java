package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "구독 유지 동의 변경 요청")
public class SubscriptionRecordUpdateRequest {
    @Schema(description = "변경할 구독 ID. 경로의 subscriptionRecordId와 같아야 한다.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long id;

    @Schema(description = "다음달에도 구독상태를 유지할 것인지에 대한 동의여부", example = "true")
    boolean agreement;
}
