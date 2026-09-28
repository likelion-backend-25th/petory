package net.likelion.bebc25.projectpatory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


// PortOne 응답에는 필드가 아주 많아서, 우리가 쓰는 필드만 받고 나머지는 무시한다 (ignoreUnknown = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortOnePaymentResponse {
    private String status;          // 결제 상태 (READY, PAID, FAILED, CANCELLED 등)
    private String id;              // 결제번호 (= 우리가 만든 paymentId)
    private String transactionId;   // PortOne 거래 ID
    private Amount amount;          // 금액 정보
    private String paidAt;          // 결제 완료 시각 (예: "2026-09-28T04:00:00Z")
    private String pgTxId;          // PG사 거래 ID
    private String receiptUrl;      // 영수증 URL
    private Failure failure;        // 실패 정보 (결제 실패일 때만 값이 있음)

    // 금액 정보: { "total": 5000, "paid": 5000, "cancelled": 0 }
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Amount {
        private Integer total;      // 결제 요청 금액
        private Integer paid;       // 실제 결제된 금액
        private Integer cancelled;  // 취소된 금액
    }

    // 실패 정보: { "pgCode": "...", "pgMessage": "잔액이 부족합니다." }
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Failure {
        private String reason;      // PortOne 실패 사유
        private String pgCode;      // PG사 실패 코드
        private String pgMessage;   // PG사 실패 메시지
    }
}
