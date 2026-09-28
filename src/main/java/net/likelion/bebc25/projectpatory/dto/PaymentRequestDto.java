package net.likelion.bebc25.projectpatory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    private Long id;
    private Long memberId;        // 결제 요청자 (현재 로그인 유저)
    private Long targetMemberId;  // 결제 대상자
    private String paymentId;     // 주문번호 (PortOne V2 paymentId) (변경: merchant_uid(V1 용어) -> paymentId)
    private String orderName;
    private String currency;      // KRW
    private Integer totalAmount;  // 예정 금액
    private String payMethod;
}