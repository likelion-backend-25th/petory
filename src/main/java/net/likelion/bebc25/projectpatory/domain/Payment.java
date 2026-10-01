package net.likelion.bebc25.projectpatory.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Schema(description = "결제 정보")
public class Payment {
    @Schema(description = "결제 테이블 PK", example = "1")
    private Long id;

    @Schema(description = "결제 요청 회원 ID", example = "10")
    private Long memberId;

    @Schema(description = "결제 대상 회원 ID", example = "20")
    private Long targetMemberId;

    @Schema(description = "주문 번호. PortOne V2 paymentId", example = "pay_20261001120000_10")
    private String paymentId;

    @Schema(description = "주문명", example = "베이직 구독")
    private String orderName;

    @Schema(description = "통화 코드", example = "KRW")
    private String currency;

    @Schema(description = "결제 예정 금액", example = "4900")
    private Integer totalAmount;

    @Schema(description = "실제 결제 완료 금액. PAID 상태일 때 값이 존재", example = "4900", nullable = true)
    private Integer paidAmount;

    @Schema(description = "결제 상품 유형", example = "automaticPayment", allowableValues = {"singlePayment", "automaticPayment"})
    private String merchandise;

    @Schema(description = "결제 상태", example = "PAID", allowableValues = {"READY", "PAID", "FAILED", "CANCELLED"})
    private String status;

    @Schema(description = "PortOne 거래 ID", example = "tx_123456789", nullable = true)
    private String transactionId;

    @Schema(description = "PG사 거래 ID", example = "pg_tx_123456789", nullable = true)
    private String pgTxId;

    @Schema(description = "영수증 URL", example = "https://example.com/receipt/123456789", nullable = true)
    private String receiptUrl;

    @Schema(description = "결제 실패 코드", example = "PAY_PROCESS_CANCELED", nullable = true)
    private String failCode;

    @Schema(description = "결제 실패 메시지", example = "사용자가 결제를 취소했습니다.", nullable = true)
    private String failMessage;

    @Schema(description = "취소 금액", example = "4900", nullable = true)
    private Integer cancelAmount;

    @Schema(description = "취소 사유", example = "사용자 요청", nullable = true)
    private String cancelReason;

    @Schema(description = "결제 생성 일시", example = "2026-10-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "결제 완료 일시", example = "2026-10-01T12:03:00", nullable = true)
    private LocalDateTime paidAt;

    @Schema(description = "결제 취소 일시", example = "2026-10-01T12:10:00", nullable = true)
    private LocalDateTime cancelledAt;

    public boolean isReady() {
        return "READY".equals(status);
    }
}
