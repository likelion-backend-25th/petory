package net.likelion.bebc25.projectpatory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.PaymentCompleteRequestDto;
import net.likelion.bebc25.projectpatory.dto.PaymentCompleteResponseDto;
import net.likelion.bebc25.projectpatory.dto.PaymentPrepareRequestDto;
import net.likelion.bebc25.projectpatory.dto.PaymentRequestDto;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/v1/payments/prepare
     * 1단계: 결제 사전 등록 엔드포인트
     */
    @PostMapping("/prepare")
    public ResponseEntity<PaymentRequestDto> preparePayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentPrepareRequestDto requestDto) { // 변경: @Valid 입력값 검증 추가

        PaymentRequestDto response = paymentService.preparePayment(userDetails.getId(), requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/payments/complete
     * 3단계: 결제 사후 검증 엔드포인트
     */
    @PostMapping("/complete")
    public ResponseEntity<PaymentCompleteResponseDto> completePayment( // 변경: String -> JSON 응답 DTO
            @AuthenticationPrincipal CustomUserDetails userDetails, // 변경: 본인 결제 확인용 JWT 인증 사용자 추가
            @Valid @RequestBody PaymentCompleteRequestDto requestDto) { // 변경: @Valid 입력값 검증 추가

        //  PAID면 200, 그 외(FAILED/CANCELLED/READY)는 400 + 상태/사유를 담은 JSON
        PaymentCompleteResponseDto response = paymentService.verifyAndCompletePayment(userDetails.getId(), requestDto);
        if ("PAID".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
