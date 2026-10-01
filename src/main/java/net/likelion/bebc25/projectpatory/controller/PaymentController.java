package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.domain.Payment;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "결제 준비, 결제 검증, PortOne 웹훅, 내 결제 내역 조회 API")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/v1/payments/prepare
     * 1단계: 결제 사전 등록 엔드포인트
     */
    @Operation(
            summary = "결제 준비",
            description = "로그인 회원의 결제 요청을 READY 상태로 사전 등록하고, 프론트엔드가 PortOne 결제창 호출에 사용할 paymentId, 주문명, 금액, 통화를 반환합니다."
    )
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentPrepareRequest requestDto) { // 변경: @Valid 입력값 검증 추가

        PaymentPrepareResponse response = paymentService.preparePayment(userDetails.getId(), requestDto);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/payments/complete
     * 3단계: 결제 사후 검증 엔드포인트
     */
    @Operation(
            summary = "결제 완료 검증",
            description = "PortOne 결제 완료 후 paymentId로 결제 정보를 검증하고 DB 결제 상태를 PAID, FAILED, CANCELLED 또는 READY 상태에 맞게 응답합니다."
    )
    @PostMapping("/complete")
    public ResponseEntity<PaymentCompleteResponse> completePayment( // String -> JSON 응답 DTO
            @AuthenticationPrincipal CustomUserDetails userDetails, //  본인 결제 확인용 JWT 인증 사용자 추가
            @Valid @RequestBody PaymentCompleteRequest requestDto) { //  @Valid 입력값 검증 추가

        //  PAID면 200, 그 외(FAILED/CANCELLED/READY)는 400 + 상태/사유를 담은 JSON
        PaymentCompleteResponse response = paymentService.verifyAndCompletePayment(userDetails.getId(), requestDto);
        if ("PAID".equals(response.status())) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * POST /api/v1/payments/webhook
     * PortOne 웹훅 수신 엔드포인트 (PortOne 서버가 호출하므로 JWT 없이 열어 둔다)
     * 200을 받지 못하면 PortOne이 최대 5번 재전송하므로, 처리했거나 무시한 경우 모두 200을 돌려준다
     */
    @Operation(
            summary = "PortOne 웹훅 수신",
            description = "PortOne 서버가 전달하는 결제 이벤트를 수신합니다. 웹훅 내용은 신뢰하지 않고 PortOne API 재조회 결과를 기준으로 결제 상태를 동기화합니다."
    )
    @PostMapping("/webhook")
    public ResponseEntity<Void> receiveWebhook(@RequestBody PortOneWebhookRequest webhook) {
        paymentService.handleWebhook(webhook);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내 결제 내역 조회",
            description = "현재 로그인한 회원이 요청한 결제 내역을 최신 결제 상태와 함께 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<List<PaymentHistoryResponse>> getMyPayments(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(
                paymentService.getMyPayments(userDetails.getId())
        );
    }
}
