package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.domain.Payment;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.exception.PaymentGatewayException;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.mapper.PaymentMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

// 변경: 신규 추가 - PaymentServiceImpl 단위 테스트
// Mapper와 RestTemplate을 가짜(Mock)로 바꿔서 실제 DB나 PortOne 없이 서비스 로직만 검사한다
@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    private static final String PORTONE_URL = "https://api.portone.io/payments/";
    private static final String PAYMENT_ID = "ORD_TEST_001";
    private static final Long MEMBER_ID = 3L;           // 결제하는 사람 (나비)
    private static final Long TARGET_MEMBER_ID = 2L;    // 후원 받는 사람 (멍치)

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    // ---------- 테스트용 데이터를 만드는 도우미 메서드 ----------

    // DB에 저장된 READY 상태 결제
    private Payment createReadyPayment(int totalAmount) {
        return Payment.builder()
                .id(100L)
                .memberId(MEMBER_ID)
                .paymentId(PAYMENT_ID)
                .totalAmount(totalAmount)
                .status("READY")
                .build();
    }

    // PortOne이 "결제 완료(PAID)"라고 알려주는 응답
    private PortOnePaymentResponse createPortOnePaidResponse(int paidAmount) {
        return PortOnePaymentResponse.builder()
                .status("PAID")
                .id(PAYMENT_ID)
                .transactionId("tx_portone_001")
                .amount(new PortOnePaymentResponse.Amount(paidAmount, paidAmount, 0))
                .paidAt("2026-09-28T04:00:00Z")
                .pgTxId("pg_tx_001")
                .receiptUrl("https://receipt.portone.io/001")
                .build();
    }

    // 가짜 RestTemplate이 PortOne 조회 요청을 받으면 response를 돌려주도록 설정
    private void givenPortOneReturns(PortOnePaymentResponse response) {
        given(restTemplate.exchange(eq(PORTONE_URL + PAYMENT_ID), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(PortOnePaymentResponse.class)))
                .willReturn(ResponseEntity.ok(response));
    }

    // =====================================================
    // 1단계: preparePayment 테스트
    // =====================================================

    @Test
    @DisplayName("결제 준비 성공 - 주문번호를 만들고 READY 결제를 저장한다")
    void preparePayment_Success() {
        // given
        PaymentPrepareRequest request = new PaymentPrepareRequest(TARGET_MEMBER_ID, "간식 쏘기", 5000, "간식 쏘기");
        given(memberMapper.findById(TARGET_MEMBER_ID)).willReturn(new Member());

        // when
        PaymentPrepareResponse result = paymentService.preparePayment(MEMBER_ID, request);

        // then (응답에는 결제창에 필요한 값만 담긴다)
        assertThat(result.paymentId()).startsWith("ORD_");
        assertThat(result.totalAmount()).isEqualTo(5000);
        assertThat(result.currency()).isEqualTo("KRW");

        // then (DB에는 결제자/대상/READY 상태까지 저장된다)
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentMapper).savePayment(captor.capture());
        Payment saved = captor.getValue();
        assertThat(saved.getPaymentId()).isEqualTo(result.paymentId());
        assertThat(saved.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(saved.getTargetMemberId()).isEqualTo(TARGET_MEMBER_ID);
        assertThat(saved.getStatus()).isEqualTo("READY");
    }

    @Test
    @DisplayName("결제 준비 실패 - 본인에게 결제하면 IllegalArgumentException")
    void preparePayment_Self() {
        // given (결제자와 후원 대상이 같음)
        PaymentPrepareRequest request = new PaymentPrepareRequest(MEMBER_ID, "간식 쏘기", 5000, "간식 쏘기");

        // when & then
        assertThatThrownBy(() -> paymentService.preparePayment(MEMBER_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인에게는 결제할 수 없습니다.");

        verify(paymentMapper, never()).savePayment(any());
    }

    @Test
    @DisplayName("결제 준비 실패 - 후원 대상 회원이 없으면 NoSuchElementException")
    void preparePayment_TargetNotFound() {
        // given
        PaymentPrepareRequest request = new PaymentPrepareRequest(999L, "간식 쏘기", 5000, "간식 쏘기");
        given(memberMapper.findById(999L)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> paymentService.preparePayment(MEMBER_ID, request))
                .isInstanceOf(NoSuchElementException.class);

        verify(paymentMapper, never()).savePayment(any());
    }

    // =====================================================
    // 3단계: verifyAndCompletePayment 테스트
    // =====================================================

    @Test
    @DisplayName("결제 검증 성공 - 금액이 같으면 PAID로 저장한다")
    void complete_Success() {
        // given (DB 5000원, PortOne 실제 결제 5000원)
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));
        givenPortOneReturns(createPortOnePaidResponse(5000));

        // when
        PaymentCompleteResponse result = paymentService.verifyAndCompletePayment(MEMBER_ID, new PaymentCompleteRequest(PAYMENT_ID));

        // then
        assertThat(result.status()).isEqualTo("PAID");
        assertThat(result.paidAmount()).isEqualTo(5000);

        // PortOne 시각 04:00(UTC)이 한국 시간 13:00으로 바뀌어 저장되는지 확인
        LocalDateTime expectedPaidAt = LocalDateTime.of(2026, 9, 28, 13, 0, 0);
        verify(paymentMapper).updatePaymentSuccess(PAYMENT_ID, "tx_portone_001", "pg_tx_001",
                "https://receipt.portone.io/001", 5000, expectedPaidAt);
    }

    @Test
    @DisplayName("위변조 - 금액이 다르면 PortOne 취소 후 CANCELLED 저장 + 취소 이력을 남긴다")
    void complete_AmountMismatch() {
        // given (DB 5000원인데 실제로는 100원만 결제됨)
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));
        givenPortOneReturns(createPortOnePaidResponse(100));

        PortOneCancelResponse.Cancellation cancellation = new PortOneCancelResponse.Cancellation(
                "SUCCEEDED", "cancel_001", "pg_cancel_001", 100,
                "https://receipt.portone.io/cancel/001", "2026-09-28T04:05:00Z");
        given(restTemplate.postForObject(eq(PORTONE_URL + PAYMENT_ID + "/cancel"), any(),
                eq(PortOneCancelResponse.class)))
                .willReturn(new PortOneCancelResponse(cancellation));

        // when
        PaymentCompleteResponse result = paymentService.verifyAndCompletePayment(MEMBER_ID, new PaymentCompleteRequest(PAYMENT_ID));

        // then
        assertThat(result.status()).isEqualTo("CANCELLED");

        // payment 테이블이 CANCELLED로 바뀌었는지
        verify(paymentMapper).updatePaymentFail(eq(PAYMENT_ID), eq("CANCELLED"), eq("AMOUNT_MISMATCH"),
                anyString(), eq(100), eq("결제 금액 위변조 감지 자동 취소"));

        // cancel_payment 테이블에 취소 이력이 저장됐는지 (payment.id = 100L)
        LocalDateTime expectedCancelledAt = LocalDateTime.of(2026, 9, 28, 13, 5, 0);
        verify(paymentMapper).insertCancelPayment(100L, "cancel_001", "pg_cancel_001", "SUCCEEDED", 100,
                "결제 금액 위변조 감지 자동 취소", "https://receipt.portone.io/cancel/001", expectedCancelledAt);

        // PAID로 저장되면 안 된다
        verify(paymentMapper, never()).updatePaymentSuccess(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("결제 실패 - PortOne 상태가 FAILED면 FAILED와 실패 사유를 저장한다")
    void complete_PortOneFailed() {
        // given
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));

        PortOnePaymentResponse failedResponse = PortOnePaymentResponse.builder()
                .status("FAILED")
                .id(PAYMENT_ID)
                .amount(new PortOnePaymentResponse.Amount(5000, 0, 0))
                .failure(new PortOnePaymentResponse.Failure("잔액 부족", "PG_001", "잔액이 부족합니다."))
                .build();
        givenPortOneReturns(failedResponse);

        // when
        PaymentCompleteResponse result = paymentService.verifyAndCompletePayment(MEMBER_ID, new PaymentCompleteRequest(PAYMENT_ID));

        // then
        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.message()).isEqualTo("잔액이 부족합니다.");
        verify(paymentMapper).updatePaymentFail(PAYMENT_ID, "FAILED", "PG_001", "잔액이 부족합니다.", null, null);
    }

    @Test
    @DisplayName("결제 미완료 - PortOne 상태가 PAID가 아니면 DB를 바꾸지 않는다")
    void complete_NotPaidYet() {
        // given
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));

        PortOnePaymentResponse readyResponse = PortOnePaymentResponse.builder()
                .status("READY")
                .id(PAYMENT_ID)
                .amount(new PortOnePaymentResponse.Amount(5000, 0, 0))
                .build();
        givenPortOneReturns(readyResponse);

        // when
        PaymentCompleteResponse result = paymentService.verifyAndCompletePayment(MEMBER_ID, new PaymentCompleteRequest(PAYMENT_ID));

        // then
        assertThat(result.status()).isEqualTo("READY");
        verify(paymentMapper, never()).updatePaymentSuccess(any(), any(), any(), any(), any(), any());
        verify(paymentMapper, never()).updatePaymentFail(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("중복 요청 - 이미 PAID인 결제는 PortOne을 다시 호출하지 않는다")
    void complete_AlreadyPaid() {
        // given
        Payment paidPayment = Payment.builder()
                .id(100L)
                .memberId(MEMBER_ID)
                .paymentId(PAYMENT_ID)
                .totalAmount(5000)
                .paidAmount(5000)
                .status("PAID")
                .build();
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(paidPayment);

        // when
        PaymentCompleteResponse result = paymentService.verifyAndCompletePayment(MEMBER_ID, new PaymentCompleteRequest(PAYMENT_ID));

        // then
        assertThat(result.status()).isEqualTo("PAID");
        assertThat(result.paidAmount()).isEqualTo(5000);
        verifyNoInteractions(restTemplate); // PortOne 호출이 한 번도 없어야 한다
    }

    @Test
    @DisplayName("검증 실패 - 존재하지 않는 주문번호면 IllegalArgumentException")
    void complete_PaymentNotFound() {
        // given
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> paymentService.verifyAndCompletePayment(MEMBER_ID, new PaymentCompleteRequest(PAYMENT_ID)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("검증 실패 - 다른 회원의 결제면 IllegalArgumentException")
    void complete_OtherMember() {
        // given (주문 주인은 3번인데 999번이 검증 요청)
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));

        // when & then
        assertThatThrownBy(() -> paymentService.verifyAndCompletePayment(999L, new PaymentCompleteRequest(PAYMENT_ID)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PortOne 404 - PortOne에 결제 내역이 없으면 NoSuchElementException")
    void complete_PortOneNotFound() {
        // given
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));
        given(restTemplate.exchange(eq(PORTONE_URL + PAYMENT_ID), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(PortOnePaymentResponse.class)))
                .willThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> paymentService.verifyAndCompletePayment(MEMBER_ID, new PaymentCompleteRequest(PAYMENT_ID)))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("PortOne 500 - PortOne 서버 오류면 PaymentGatewayException")
    void complete_PortOneServerError() {
        // given
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));
        given(restTemplate.exchange(eq(PORTONE_URL + PAYMENT_ID), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(PortOnePaymentResponse.class)))
                .willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // when & then
        assertThatThrownBy(() -> paymentService.verifyAndCompletePayment(MEMBER_ID, new PaymentCompleteRequest(PAYMENT_ID)))
                .isInstanceOf(PaymentGatewayException.class);

        verify(paymentMapper, never()).updatePaymentFail(any(), any(), any(), any(), any(), any());
    }

    // =====================================================
    // 웹훅: handleWebhook 테스트
    // =====================================================

    private PortOneWebhookRequest createWebhook(String type, String paymentId) {
        return new PortOneWebhookRequest(type, "2026-09-28T04:00:00Z",
                new PortOneWebhookRequest.Data(paymentId, "store-test", "tx_portone_001"));
    }

    @Test
    @DisplayName("웹훅 Transaction.Paid - PortOne 조회 결과 금액이 같으면 PAID로 저장한다")
    void webhook_Paid() {
        // given
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));
        givenPortOneReturns(createPortOnePaidResponse(5000));

        // when
        paymentService.handleWebhook(createWebhook("Transaction.Paid", PAYMENT_ID));

        // then
        LocalDateTime expectedPaidAt = LocalDateTime.of(2026, 9, 28, 13, 0, 0);
        verify(paymentMapper).updatePaymentSuccess(PAYMENT_ID, "tx_portone_001", "pg_tx_001",
                "https://receipt.portone.io/001", 5000, expectedPaidAt);
    }

    @Test
    @DisplayName("웹훅 - /complete가 먼저 처리해서 이미 PAID면 PortOne을 다시 호출하지 않는다")
    void webhook_AlreadyPaid() {
        // given
        Payment paidPayment = Payment.builder()
                .id(100L)
                .memberId(MEMBER_ID)
                .paymentId(PAYMENT_ID)
                .totalAmount(5000)
                .paidAmount(5000)
                .status("PAID")
                .build();
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(paidPayment);

        // when
        paymentService.handleWebhook(createWebhook("Transaction.Paid", PAYMENT_ID));

        // then
        verifyNoInteractions(restTemplate);
        verify(paymentMapper, never()).updatePaymentSuccess(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("웹훅 - 처리하지 않는 이벤트(Transaction.Ready)는 DB도 PortOne도 건드리지 않는다")
    void webhook_IgnoredType() {
        // when
        paymentService.handleWebhook(createWebhook("Transaction.Ready", PAYMENT_ID));

        // then
        verifyNoInteractions(paymentMapper);
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("웹훅 - DB에 없는 주문번호면 예외 없이 무시한다 (PortOne 재전송 방지)")
    void webhook_PaymentNotFound() {
        // given
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(null);

        // when
        paymentService.handleWebhook(createWebhook("Transaction.Paid", PAYMENT_ID));

        // then
        verifyNoInteractions(restTemplate);
    }

    @Test
    @DisplayName("웹훅 - PortOne 서버 오류면 PaymentGatewayException (502 응답 -> PortOne이 재전송)")
    void webhook_PortOneServerError() {
        // given
        given(paymentMapper.findByPaymentIdForUpdate(PAYMENT_ID)).willReturn(createReadyPayment(5000));
        given(restTemplate.exchange(eq(PORTONE_URL + PAYMENT_ID), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(PortOnePaymentResponse.class)))
                .willThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // when & then
        assertThatThrownBy(() -> paymentService.handleWebhook(createWebhook("Transaction.Paid", PAYMENT_ID)))
                .isInstanceOf(PaymentGatewayException.class);
    }
}
