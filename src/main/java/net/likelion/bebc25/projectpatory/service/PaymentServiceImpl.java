package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.exception.PaymentGatewayException;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final String PORTONE_API_URL = "https://api.portone.io/payments/";
    private static final String AMOUNT_MISMATCH_REASON = "결제 금액 위변조 감지 자동 취소";

    private final PaymentMapper paymentMapper;
    private final MemberMapper memberMapper;    // 결제 대상 회원 존재 여부 확인용
    private final RestTemplate restTemplate;    // 직접 생성 대신 Bean 주입 (RestTemplateConfig)

    @Value("${portone.api.secret}")
    private String apiSecret;

    // =================================================================
    // 1단계: 결제 사전 등록
    // =================================================================
    @Override
    @Transactional
    public PaymentRequestDto preparePayment(Long currentMemberId, PaymentPrepareRequestDto requestDto) {
        Long targetMemberId = requestDto.getTargetMemberId();

        // 1. 본인에게는 후원할 수 없다
        if (currentMemberId.equals(targetMemberId)) {
            throw new IllegalArgumentException("본인에게는 결제할 수 없습니다.");
        }

        // 2. 후원 대상 회원이 실제로 있는지 확인한다 (없으면 404)
        Member targetMember = memberMapper.findById(targetMemberId);
        if (targetMember == null) {
            throw new NoSuchElementException("결제 대상 회원을 찾을 수 없습니다. id=" + targetMemberId);
        }

        // 3. 주문번호를 만든다 (예: ORD_1727500000000_1a2b3c4d)
        String randomText = UUID.randomUUID().toString().substring(0, 8);
        String paymentId = "ORD_" + System.currentTimeMillis() + "_" + randomText;

        // 4. DB에 READY 상태로 저장한다 (결제 예정 금액을 서버에 기록해 두는 것이 핵심)
        PaymentRequestDto paymentRequest = PaymentRequestDto.builder()
                .memberId(currentMemberId)
                .targetMemberId(targetMemberId)
                .paymentId(paymentId)
                .orderName(requestDto.getOrderName())
                .currency("KRW")
                .totalAmount(requestDto.getTotalAmount())
                .payMethod(requestDto.getPayMethod())
                .build();
        paymentMapper.savePayment(paymentRequest);

        log.info("[결제 준비 완료] 주문번호: {}, 결제자: {}, 예정금액: {}원",
                paymentId, currentMemberId, requestDto.getTotalAmount());
        return paymentRequest;
    }

    // =================================================================
    // 3단계: 결제 사후 검증
    // boolean 대신 결과 DTO(PaymentCompleteResponseDto)를 반환
    // =================================================================
    @Override
    @Transactional
    public PaymentCompleteResponseDto verifyAndCompletePayment(Long currentMemberId, PaymentCompleteRequestDto requestDto) {
        String paymentId = requestDto.getPaymentId();

        // 1. DB에서 결제 정보를 조회한다
        //    FOR UPDATE로 이 행을 잠가서, 같은 결제로 요청이 동시에 두 번 와도 한 번씩 차례대로 처리되게 한다
        PaymentResponseDto payment = paymentMapper.findByPaymentIdForUpdate(paymentId);

        // 2. 주문이 없거나 내 주문이 아니면 거절한다
        if (payment == null) {
            throw new IllegalArgumentException("존재하지 않는 주문 번호입니다: " + paymentId);
        }
        if (!payment.getMemberId().equals(currentMemberId)) {
            // 다른 사람 주문이 있다는 사실도 알려주지 않으려고 같은 메시지를 쓴다
            throw new IllegalArgumentException("존재하지 않는 주문 번호입니다: " + paymentId);
        }

        // 3. 이미 처리된 결제(PAID, FAILED, CANCELLED)면 PortOne에 다시 묻지 않고 현재 상태를 돌려준다
        if (!payment.getStatus().equals("READY")) {
            log.info("[이미 처리된 결제] 주문번호: {}, 상태: {}", paymentId, payment.getStatus());
            return new PaymentCompleteResponseDto(paymentId, payment.getStatus(), payment.getPaidAmount(),
                    "이미 처리된 결제입니다.");
        }

        // 4. PortOne 서버에 실제 결제 내역을 물어본다
        PortOnePaymentResponse portOnePayment = getPortOnePayment(paymentId);
        String portOneStatus = portOnePayment.getStatus();

        // 5. 결제가 실패했으면 FAILED로 저장한다
        if (portOneStatus.equals("FAILED")) {
            return handleFailedPayment(paymentId, portOnePayment);
        }

        // 6. 아직 결제가 끝나지 않았으면 DB는 그대로 두고 READY를 돌려준다
        if (!portOneStatus.equals("PAID")) {
            log.info("[결제 미완료] 주문번호: {}, PortOne 상태: {}", paymentId, portOneStatus);
            return new PaymentCompleteResponseDto(paymentId, "READY", null,
                    "결제가 아직 완료되지 않았습니다. (PortOne 상태: " + portOneStatus + ")");
        }

        // 7. 금액을 비교한다 (DB에 저장한 예정 금액 vs PortOne에서 실제 결제된 금액)
        Integer expectedAmount = payment.getTotalAmount();
        Integer actualPaidAmount = portOnePayment.getAmount().getTotal();

        if (!expectedAmount.equals(actualPaidAmount)) {
            // 금액이 다르면 위변조로 보고 자동 취소한다
            return handleAmountMismatch(payment, actualPaidAmount);
        }

        // 8. 모든 검사를 통과하면 PAID로 저장한다
        LocalDateTime paidAt = toKoreanTime(portOnePayment.getPaidAt());
        paymentMapper.updatePaymentSuccess(
                paymentId,
                portOnePayment.getTransactionId(),
                portOnePayment.getPgTxId(),
                portOnePayment.getReceiptUrl(),
                actualPaidAmount,
                paidAt
        );

        log.info("[결제 검증 성공] 주문번호: {}, 결제금액: {}원", paymentId, actualPaidAmount);
        return new PaymentCompleteResponseDto(paymentId, "PAID", actualPaidAmount, "결제 성공 및 검증이 완료되었습니다.");
    }

    // 결제 실패 처리: FAILED와 PG사 실패 사유를 저장한다
    private PaymentCompleteResponseDto handleFailedPayment(String paymentId, PortOnePaymentResponse portOnePayment) {
        String failCode = null;
        String failMessage = null;

        PortOnePaymentResponse.Failure failure = portOnePayment.getFailure();
        if (failure != null) {
            failCode = failure.getPgCode();
            failMessage = failure.getPgMessage();
        }

        paymentMapper.updatePaymentFail(paymentId, "FAILED", failCode, failMessage, null, null);
        log.warn("[결제 실패] 주문번호: {}, 사유: {}", paymentId, failMessage);

        String message = "결제에 실패했습니다.";
        if (failMessage != null) {
            message = failMessage;
        }
        return new PaymentCompleteResponseDto(paymentId, "FAILED", null, message);
    }

    // 금액 위변조 처리: PortOne 취소 요청 -> payment를 CANCELLED로 변경 -> cancel_payment에 이력 저장
    private PaymentCompleteResponseDto handleAmountMismatch(PaymentResponseDto payment, Integer actualPaidAmount) {
        String paymentId = payment.getPaymentId();
        log.error("[위변조 감지] 주문번호: {}, DB 예정금액: {}원, 실제 결제금액: {}원 -> 자동 취소",
                paymentId, payment.getTotalAmount(), actualPaidAmount);

        // 1. PortOne에 결제 취소를 요청한다 (사용자에게 돈을 돌려준다)
        PortOneCancelResponse.Cancellation cancellation = cancelPortOnePayment(paymentId, AMOUNT_MISMATCH_REASON);

        // 2. payment 테이블을 CANCELLED로 바꾼다
        paymentMapper.updatePaymentFail(paymentId, "CANCELLED", "AMOUNT_MISMATCH",
                "DB 예정금액과 PG 실결제 금액 불일치", actualPaidAmount, AMOUNT_MISMATCH_REASON);

        // 3. cancel_payment 테이블에 취소 이력을 남긴다
        //    PortOne 취소 응답이 비어 있을 수도 있어서 기본값을 먼저 넣어 둔다
        String cancellationId = null;
        String pgCancellationId = null;
        String cancelStatus = "SUCCEEDED";
        Integer cancelAmount = actualPaidAmount;
        String receiptUrl = null;
        LocalDateTime cancelledAt = LocalDateTime.now();

        if (cancellation != null) {
            cancellationId = cancellation.getId();
            pgCancellationId = cancellation.getPgCancellationId();
            receiptUrl = cancellation.getReceiptUrl();
            if (cancellation.getStatus() != null) {
                cancelStatus = cancellation.getStatus();
            }
            if (cancellation.getTotalAmount() != null) {
                cancelAmount = cancellation.getTotalAmount();
            }
            if (cancellation.getCancelledAt() != null) {
                cancelledAt = toKoreanTime(cancellation.getCancelledAt());
            }
        }

        // cancel_payment.payment_id 컬럼에는 주문번호(문자열)가 아니라 payment.id(숫자 PK)가 들어간다
        paymentMapper.insertCancelPayment(payment.getId(), cancellationId, pgCancellationId,
                cancelStatus, cancelAmount, AMOUNT_MISMATCH_REASON, receiptUrl, cancelledAt);

        return new PaymentCompleteResponseDto(paymentId, "CANCELLED", null,
                "결제 금액 위변조 시도가 감지되어 결제가 취소되었습니다.");
    }

    // =================================================================
    // PortOne V2 REST API 호출
    // =================================================================

    // 결제 단건 조회: GET https://api.portone.io/payments/{paymentId}
    private PortOnePaymentResponse getPortOnePayment(String paymentId) {
        String url = PORTONE_API_URL + paymentId;
        HttpEntity<Void> request = new HttpEntity<>(createPortOneHeaders());

        ResponseEntity<PortOnePaymentResponse> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.GET, request, PortOnePaymentResponse.class);
        } catch (HttpClientErrorException e) {
            // 4xx 에러: 404면 PortOne에 결제 내역이 없다는 뜻
            if (e.getStatusCode().value() == 404) {
                throw new NoSuchElementException("PortOne에서 결제 내역을 찾을 수 없습니다: " + paymentId);
            }
            throw new PaymentGatewayException("PortOne 결제 조회에 실패했습니다.", e);
        } catch (RestClientException e) {
            // 5xx 에러, 타임아웃, 네트워크 오류 등
            throw new PaymentGatewayException("PortOne 결제 조회에 실패했습니다.", e);
        }

        // 응답 내용이 비어 있으면 검증을 할 수 없으므로 실패로 본다
        PortOnePaymentResponse body = response.getBody();
        if (body == null || body.getStatus() == null || body.getAmount() == null) {
            throw new PaymentGatewayException("PortOne 결제 조회 응답이 올바르지 않습니다.");
        }
        return body;
    }

    // 결제 취소: POST https://api.portone.io/payments/{paymentId}/cancel
    private PortOneCancelResponse.Cancellation cancelPortOnePayment(String paymentId, String reason) {
        String url = PORTONE_API_URL + paymentId + "/cancel";

        HttpHeaders headers = createPortOneHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PortOneCancelRequest> request = new HttpEntity<>(new PortOneCancelRequest(reason), headers);

        PortOneCancelResponse response;
        try {
            response = restTemplate.postForObject(url, request, PortOneCancelResponse.class);
        } catch (RestClientException e) {
            throw new PaymentGatewayException("PortOne 결제 취소에 실패했습니다.", e);
        }

        if (response == null) {
            return null;
        }
        return response.getCancellation();
    }

    // PortOne V2 인증 헤더: "Authorization: PortOne {API Secret}"
    private HttpHeaders createPortOneHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "PortOne " + apiSecret);
        return headers;
    }

    // PortOne 시각(예: "2026-09-28T04:00:00Z", UTC 기준)을 한국 시간으로 바꾼다
    private LocalDateTime toKoreanTime(String isoDateTime) {
        if (isoDateTime == null) {
            return null;
        }
        OffsetDateTime utcTime = OffsetDateTime.parse(isoDateTime);
        return utcTime.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }
}
