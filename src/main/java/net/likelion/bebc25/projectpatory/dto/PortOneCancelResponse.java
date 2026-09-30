package net.likelion.bebc25.projectpatory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// PortOne V2 결제 취소 응답을 담는 DTO (cancel_payment 이력 저장에 사용)
// 응답 모양: { "cancellation": { "status": "SUCCEEDED", "id": "...", ... } }
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneCancelResponse(
        Cancellation cancellation
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cancellation(
            String status,              // 취소 상태 (REQUESTED, SUCCEEDED, FAILED)
            String id,                  // PortOne 취소 ID
            String pgCancellationId,    // PG사 취소 ID
            Integer totalAmount,        // 취소 금액
            String receiptUrl,          // 취소 영수증 URL
            String cancelledAt          // 취소 완료 시각
    ) {}
}
