package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import net.likelion.bebc25.projectpatory.domain.Payment;

import java.time.LocalDateTime;

@Schema(description = "결제 내역 조회 응답")
public record PaymentHistoryResponse(
        @Schema(description = "주문 번호. PortOne V2 paymentId", example = "pay_20261001120000_10")
        String paymentId,

        @Schema(description = "결제 대상 회원 ID", example = "20")
        Long targetMemberId,

        @Schema(description = "주문명", example = "베이직 구독")
        String orderName,

        @Schema(description = "통화 코드", example = "KRW")
        String currency,

        @Schema(description = "결제 예정 금액", example = "4900")
        Integer totalAmount,

        @Schema(description = "실제 결제 완료 금액. PAID 상태일 때 값이 존재", example = "4900", nullable = true)
        Integer paidAmount,

        @Schema(description = "결제 상태", example = "PAID", allowableValues = {"READY", "PAID", "FAILED", "CANCELLED"})
        String status,

        @Schema(description = "결제 생성 일시", example = "2026-10-01T12:00:00")
        LocalDateTime createdAt,

        @Schema(description = "결제 완료 일시", example = "2026-10-01T12:03:00", nullable = true)
        LocalDateTime paidAt,

        @Schema(description = "결제 취소 일시", example = "2026-10-01T12:10:00", nullable = true)
        LocalDateTime cancelledAt
) {

    public static PaymentHistoryResponse from(Payment payment) {
        return new PaymentHistoryResponse(
                payment.getPaymentId(),
                payment.getTargetMemberId(),
                payment.getOrderName(),
                payment.getCurrency(),
                payment.getTotalAmount(),
                payment.getPaidAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getPaidAt(),
                payment.getCancelledAt()
        );
    }
}
