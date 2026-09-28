package net.likelion.bebc25.projectpatory.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompleteRequestDto {
    @NotBlank(message = "주문번호(paymentId)는 필수입니다.") // 입력값 검증 추가
    private String paymentId;     // 백엔드가 prepare 단계에서 생성한 주문번호 (PortOne V2 paymentId)
}
