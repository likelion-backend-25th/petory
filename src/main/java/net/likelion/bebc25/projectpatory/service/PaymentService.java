package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PaymentCompleteRequestDto;
import net.likelion.bebc25.projectpatory.dto.PaymentCompleteResponseDto;
import net.likelion.bebc25.projectpatory.dto.PaymentPrepareRequestDto;
import net.likelion.bebc25.projectpatory.dto.PaymentRequestDto;
import net.likelion.bebc25.projectpatory.dto.PortOneWebhookRequest;

public interface PaymentService {

    /**
     * 1단계: 결제 사전 등록 (Pre-validation)
     * 주문 고유 번호(paymentId)를 발급하고 예정 금액을 DB에 READY 상태로 저장합니다. (변경: merchant_uid(V1 용어) -> paymentId)
     */
    PaymentRequestDto preparePayment(Long currentMemberId, PaymentPrepareRequestDto requestDto);

    // 3단계: V2 사후 검증 및 금액 위변조 차단
    // 본인 결제 확인을 위해 로그인 사용자 ID(currentMemberId) 파라미터 추가
    // 반환 타입 boolean -> PaymentCompleteResponseDto (결제 상태/금액/메시지를 JSON으로 응답)
    PaymentCompleteResponseDto verifyAndCompletePayment(Long currentMemberId, PaymentCompleteRequestDto requestDto);

    // PortOne 웹훅 처리: 웹훅 내용은 믿지 않고 PortOne API로 다시 조회해서 DB를 맞춘다
    void handleWebhook(PortOneWebhookRequest webhook);
}