package net.likelion.bebc25.projectpatory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    private Long id;
    private Long memberId;
    private Long targetMemberId;
    private String paymentId;
    private String orderName;
    private String currency;
    private Integer totalAmount;
    private Integer paidAmount;
    private String payMethod;
    private String status;         // READY, PAID, FAILED, CANCELLED
    private String transactionId;
    private String pgTxId;
    private String receiptUrl;
    private String failCode;
    private String failMessage;
    private Integer cancelAmount;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
}