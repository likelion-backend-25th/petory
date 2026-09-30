package net.likelion.bebc25.projectpatory.dto;

import net.likelion.bebc25.projectpatory.domain.Payment;

// /prepare 응답: 프론트엔드가 PortOne 결제창을 띄우는 데 필요한 값만 내려준다
public record PaymentPrepareResponse(
        String paymentId,       // PortOne SDK requestPayment의 paymentId로 그대로 사용
        String orderName,
        Integer totalAmount,
        String currency
) {
    public static PaymentPrepareResponse from(Payment payment) {
        return new PaymentPrepareResponse(
                payment.getPaymentId(),
                payment.getOrderName(),
                payment.getTotalAmount(),
                payment.getCurrency()
        );
    }
}
