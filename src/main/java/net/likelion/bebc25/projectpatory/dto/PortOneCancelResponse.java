package net.likelion.bebc25.projectpatory.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// PortOne V2 결제 취소 응답을 담는 DTO (cancel_payment 이력 저장에 사용)
// 응답 모양: { "cancellation": { "status": "SUCCEEDED", "id": "...", ... } }
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortOneCancelResponse {
    private Cancellation cancellation;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cancellation {
        private String status;              // 취소 상태 (REQUESTED, SUCCEEDED, FAILED)
        private String id;                  // PortOne 취소 ID
        private String pgCancellationId;    // PG사 취소 ID
        private Integer totalAmount;        // 취소 금액
        private String receiptUrl;          // 취소 영수증 URL
        private String cancelledAt;         // 취소 완료 시각
    }
}
