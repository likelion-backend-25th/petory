package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "결제 완료 검증 요청")
public record PaymentCompleteRequest(
        @Schema(
                description = "백엔드 prepare 단계에서 생성한 주문 번호. PortOne V2 paymentId",
                example = "pay_20261001120000_10",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "주문번호(paymentId)는 필수입니다.")
        String paymentId
) {}
