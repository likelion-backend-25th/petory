package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionRecordUpdateRequest {
    Long id;
    @Schema(description = "다음달에도 구독상태를 유지할 것인지에 대한 동의여부", example = "true")
    boolean agreement;
}
