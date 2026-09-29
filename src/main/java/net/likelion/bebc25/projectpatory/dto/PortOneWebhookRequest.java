package net.likelion.bebc25.projectpatory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// PortOne 웹훅 본문 (웹훅 버전 2024-04-25)
// 예: { "type": "Transaction.Paid", "timestamp": "...", "data": { "paymentId": "ORD_...", "storeId": "...", "transactionId": "..." } }
// PortOne이 예고 없이 필드를 추가할 수 있어서 모르는 필드는 무시한다 (ignoreUnknown = true)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortOneWebhookRequest {
    private String type;        // 이벤트 종류 (Transaction.Paid, Transaction.Failed 등)
    private String timestamp;   // 이벤트 발생 시각
    private Data data;          // 이벤트 내용

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String paymentId;       // 우리가 만든 주문번호
        private String storeId;         // PortOne 상점 ID
        private String transactionId;   // PortOne 결제 시도 번호
    }
}
