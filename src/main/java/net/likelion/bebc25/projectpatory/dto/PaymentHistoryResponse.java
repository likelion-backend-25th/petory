package net.likelion.bebc25.projectpatory.dto;

import net.likelion.bebc25.projectpatory.domain.Payment;

import java.time.LocalDateTime;

public record PaymentHistoryResponse(
        String paymentId,
        Long targetMemberId,
        String orderName,
        String currency,
        Integer totalAmount,
        Integer paidAmount,
        String status,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
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