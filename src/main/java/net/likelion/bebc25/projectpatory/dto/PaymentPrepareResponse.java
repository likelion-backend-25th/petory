package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import net.likelion.bebc25.projectpatory.domain.Payment;

@Schema(description = "결제 준비 응답")
public record PaymentPrepareResponse(
        @Schema(description = "PortOne SDK requestPayment에 전달할 paymentId", example = "pay_20261001120000_10")
        String paymentId,

        @Schema(description = "주문명", example = "베이직 구독")
        String orderName,

        @Schema(description = "결제 예정 금액", example = "4900")
        Integer totalAmount,

        @Schema(description = "통화 코드", example = "KRW")
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
