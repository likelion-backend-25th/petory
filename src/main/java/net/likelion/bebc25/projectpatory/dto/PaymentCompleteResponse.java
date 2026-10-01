package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 완료 검증 응답")
public record PaymentCompleteResponse(
        @Schema(description = "주문 번호. PortOne V2 paymentId", example = "pay_20261001120000_10")
        String paymentId,

        @Schema(description = "결제 상태", example = "PAID", allowableValues = {"READY", "PAID", "FAILED", "CANCELLED"})
        String status,

        @Schema(description = "실제 결제 완료 금액. PAID 상태일 때 값이 존재", example = "4900", nullable = true)
        Integer paidAmount,

        @Schema(description = "화면에 표시할 안내 메시지", example = "결제가 완료되었습니다.")
        String message
) {}
