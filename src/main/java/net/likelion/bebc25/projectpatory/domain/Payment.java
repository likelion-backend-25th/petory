package net.likelion.bebc25.projectpatory.domain;

import lombok.*;

import java.time.LocalDateTime;

// payment 테이블 한 줄
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Payment {
    private Long id;
    private Long memberId;          // 결제자 (로그인 회원)
    private Long targetMemberId;    // 후원 대상 회원
    private String paymentId;       // 주문번호 (PortOne V2 paymentId)
    private String orderName;
    private String currency;        // KRW
    private Integer totalAmount;    // 결제 예정 금액
    private Integer paidAmount;     // 실제 결제 금액 (PAID일 때만)
    private String payMethod;
    private String status;          // READY, PAID, FAILED, CANCELLED
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

    // 아직 결과가 확정되지 않은 결제인지 (READY만 PAID/FAILED/CANCELLED로 바뀔 수 있다)
    public boolean isReady() {
        return "READY".equals(status);
    }
}
