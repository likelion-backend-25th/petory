package net.likelion.bebc25.projectpatory.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentCompleteRequest(
        @NotBlank(message = "주문번호(paymentId)는 필수입니다.") // 입력값 검증 추가
        String paymentId        // 백엔드가 prepare 단계에서 생성한 주문번호 (PortOne V2 paymentId)
) {}
