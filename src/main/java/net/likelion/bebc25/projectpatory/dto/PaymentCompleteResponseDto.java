package net.likelion.bebc25.projectpatory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

//complete 응답을 문자열 대신 JSON으로 반환
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompleteResponseDto {
    private String paymentId;
    private String status;          // PAID, FAILED, CANCELLED, READY(아직 결제 안 됨)
    private Integer paidAmount;     // PAID일 때만 값이 있음
    private String message;         // 화면에 보여줄 안내 메시지
}
