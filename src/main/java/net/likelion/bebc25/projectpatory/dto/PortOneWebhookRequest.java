package net.likelion.bebc25.projectpatory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// PortOne 웹훅 본문 (웹훅 버전 2024-04-25)
// 예: { "type": "Transaction.Paid", "timestamp": "...", "data": { "paymentId": "ORD_...", "storeId": "...", "transactionId": "..." } }
// PortOne이 예고 없이 필드를 추가할 수 있어서 모르는 필드는 무시한다 (ignoreUnknown = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneWebhookRequest(
        String type,        // 이벤트 종류 (Transaction.Paid, Transaction.Failed 등)
        String timestamp,   // 이벤트 발생 시각
        Data data           // 이벤트 내용
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(
            String paymentId,       // 우리가 만든 주문번호
            String storeId,         // PortOne 상점 ID
            String transactionId    // PortOne 결제 시도 번호
    ) {}
}
