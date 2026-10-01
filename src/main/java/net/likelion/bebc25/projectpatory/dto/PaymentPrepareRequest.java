package net.likelion.bebc25.projectpatory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaymentPrepareRequest {
    @NotNull(message = "결제 대상 회원 ID는 필수입니다.")
    Long targetMemberId;    // 결제/구독 대상 크리에이터 ID

    @NotBlank(message = "주문명은 필수입니다.")
    @Size(max = 100, message = "주문명은 100자 이하여야 합니다.")
    String orderName;       // 주문명

    @NotNull(message = "결제 금액은 필수입니다.")
    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    Integer totalAmount;    // 결제 예정 금액

    @NotBlank(message = "결제 상품은 필수입니다.")
    @Size(max = 30, message = "결제 상품은 30자 이하여야 합니다.")
    String merchandise;      // 결제 상품 (간식 쏘기, 펫 클럽 구독)
}
