package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "결제 준비 요청")
public record PaymentPrepareRequest(
        @Schema(description = "결제 또는 구독 대상 크리에이터 회원 ID", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "결제 대상 회원 ID는 필수입니다.")
        Long targetMemberId,

        @Schema(description = "주문명", example = "베이직 구독", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "주문명은 필수입니다.")
        @Size(max = 100, message = "주문명은 100자 이하여야 합니다.")
        String orderName,

        @Schema(description = "결제 예정 금액", example = "4900", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        Integer totalAmount,

        @Schema(
                description = "결제 상품 유형. singlePayment는 간식 선물, automaticPayment는 구독 결제",
                example = "automaticPayment",
                allowableValues = {"singlePayment", "automaticPayment"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "결제 상품은 필수입니다.")
        @Pattern(regexp = "singlePayment|automaticPayment", message = "결제 상품은 singlePayment 또는 automaticPayment만 가능합니다.")
        String merchandise
) {}
