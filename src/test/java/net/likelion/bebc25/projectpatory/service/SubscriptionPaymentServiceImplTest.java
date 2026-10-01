package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.BillingKey;
import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.domain.SubscriptionRecord;
import net.likelion.bebc25.projectpatory.dto.MySubscriptionsResponse;
import net.likelion.bebc25.projectpatory.dto.PaymentCompleteResponse;
import net.likelion.bebc25.projectpatory.dto.PaymentPrepareResponse;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordUpdateRequest;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import net.likelion.bebc25.projectpatory.mapper.SubscriptionMapper;
import net.likelion.bebc25.projectpatory.mapper.SubscriptionPaymentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionPaymentServiceImplTest {

    @Mock
    private SubscriptionPaymentMapper subscriptionPaymentMapper;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private MemberMapper memberMapper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RestClient restClient;

    @InjectMocks
    private SubscriptionPaymentServiceImpl subscriptionPaymentService;

    private RestClient.RequestHeadersUriSpec<?> getSpec;
    private RestClient.RequestBodyUriSpec postSpec;
    private RestClient.ResponseSpec getResponse;
    private RestClient.ResponseSpec postResponse;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUpRestClient() {
        getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        postSpec = mock(RestClient.RequestBodyUriSpec.class);
        getResponse = mock(RestClient.ResponseSpec.class);
        postResponse = mock(RestClient.ResponseSpec.class);
    }

    @Test
    @DisplayName("결제가 PAID이면 구독 행을 저장한다")
    void createSubscriptionRecord_paid() {
        Subscription plan = activePlan();
        SubscriptionRecordCreateRequest request = createRequest();
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(plan);
        given(subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(4L)).willReturn(List.of());
        stubBillingKeyLookup(issuedKey("bk_1"));
        given(paymentService.preparePayment(any(), any())).willReturn(prepared());
        stubBillingKeyPayment();
        given(paymentService.verifyAndCompletePayment(any(), any()))
                .willReturn(new PaymentCompleteResponse("ORD_1", "PAID", 4900, "성공"));

        subscriptionPaymentService.createSubscriptionRecord(2L, request, 4L);

        verify(subscriptionPaymentMapper).createSubscriptionRecord(4L, request, LocalDate.now().plusMonths(1));
    }

    @Test
    @DisplayName("없는 플랜이면 구독을 저장하지 않는다")
    void createSubscriptionRecord_missingPlan() {
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(null);

        assertThatThrownBy(() -> subscriptionPaymentService.createSubscriptionRecord(2L, createRequest(), 4L))
                .isInstanceOf(NoSuchElementException.class);

        verify(subscriptionPaymentMapper, never()).createSubscriptionRecord(any(), any(), any());
    }

    @Test
    @DisplayName("삭제됐거나 삭제 예정인 플랜이면 구독을 저장하지 않는다")
    void createSubscriptionRecord_inactivePlan() {
        Subscription deleted = activePlan();
        deleted.setStatus("DELETED");
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(deleted);

        assertThatThrownBy(() -> subscriptionPaymentService.createSubscriptionRecord(2L, createRequest(), 4L))
                .isInstanceOf(NoSuchElementException.class);

        Subscription pending = activePlan();
        pending.setStatus("PENDING_DELETION");
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(pending);

        assertThatThrownBy(() -> subscriptionPaymentService.createSubscriptionRecord(2L, createRequest(), 4L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("본인 플랜은 구독할 수 없다")
    void createSubscriptionRecord_self() {
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());

        assertThatThrownBy(() -> subscriptionPaymentService.createSubscriptionRecord(4L, createRequest(), 4L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인의 구독상품을 구독할 수 없습니다.");
    }

    @Test
    @DisplayName("결제 대상과 플랜 주인이 다르면 구독하지 않는다")
    void createSubscriptionRecord_targetMismatch() {
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());
        SubscriptionRecordCreateRequest request = SubscriptionRecordCreateRequest.builder()
                .targetMemberId(9L)
                .planId(1L)
                .billingKey("bk_1")
                .build();

        assertThatThrownBy(() -> subscriptionPaymentService.createSubscriptionRecord(2L, request, 4L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 진행 중 오류가 발생하였습니다.");
    }

    @Test
    @DisplayName("이미 구독한 플랜이면 다시 결제하지 않는다")
    void createSubscriptionRecord_duplicate() {
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());
        given(subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(4L))
                .willReturn(List.of(SubscriptionRecord.builder().planId(1L).build()));

        assertThatThrownBy(() -> subscriptionPaymentService.createSubscriptionRecord(2L, createRequest(), 4L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 구독한 상품입니다.");

        verify(paymentService, never()).preparePayment(any(), any());
    }

    @Test
    @DisplayName("발급 상태가 아닌 빌링키는 거절한다")
    void createSubscriptionRecord_billingKeyNotIssued() {
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());
        given(subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(4L)).willReturn(List.of());
        stubBillingKeyLookup(BillingKey.builder().status("DELETED").billingKey("bk_1").build());

        assertThatThrownBy(() -> subscriptionPaymentService.createSubscriptionRecord(2L, createRequest(), 4L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 결제수단입니다.");

        verify(paymentService, never()).preparePayment(any(), any());
    }

    @Test
    @DisplayName("결제가 PAID가 아니면 구독 행을 저장하지 않는다")
    void createSubscriptionRecord_notPaid() {
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());
        given(subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(4L)).willReturn(List.of());
        stubBillingKeyLookup(issuedKey("bk_1"));
        given(paymentService.preparePayment(any(), any())).willReturn(prepared());
        stubBillingKeyPayment();
        given(paymentService.verifyAndCompletePayment(any(), any()))
                .willReturn(new PaymentCompleteResponse("ORD_1", "FAILED", null, "실패"));

        assertThatThrownBy(() -> subscriptionPaymentService.createSubscriptionRecord(2L, createRequest(), 4L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 진행 도중 오류가 발생하였습니다.");

        verify(subscriptionPaymentMapper, never()).createSubscriptionRecord(any(), any(), any());
    }

    @Test
    @DisplayName("본인 구독의 동의 여부만 바꾼다")
    void updateSubscriptionRecord_success() {
        given(subscriptionPaymentMapper.getSubscriptionRecordById(1L)).willReturn(activeRecord(4L));
        SubscriptionRecordUpdateRequest request = new SubscriptionRecordUpdateRequest(1L, false);

        subscriptionPaymentService.updateSubscriptionRecord(request, 4L, 4L, 1L);

        verify(subscriptionPaymentMapper).updateSubscriptionRecord(request);
    }

    @Test
    @DisplayName("다른 회원의 구독 동의는 바꿀 수 없다")
    void updateSubscriptionRecord_denied() {
        given(subscriptionPaymentMapper.getSubscriptionRecordById(1L)).willReturn(activeRecord(4L));
        SubscriptionRecordUpdateRequest request = new SubscriptionRecordUpdateRequest(1L, false);

        assertThatThrownBy(() -> subscriptionPaymentService.updateSubscriptionRecord(request, 9L, 9L, 1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(subscriptionPaymentMapper, never()).updateSubscriptionRecord(any());
    }

    @Test
    @DisplayName("이미 해지한 구독의 동의는 바꿀 수 없다")
    void updateSubscriptionRecord_cancelled() {
        SubscriptionRecord cancelled = activeRecord(4L);
        cancelled.setStatus("CANCELLED");
        given(subscriptionPaymentMapper.getSubscriptionRecordById(1L)).willReturn(cancelled);

        assertThatThrownBy(() -> subscriptionPaymentService.updateSubscriptionRecord(
                new SubscriptionRecordUpdateRequest(1L, false), 4L, 4L, 1L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("본인 구독을 해지한다")
    void cancelSubscription_success() {
        given(subscriptionPaymentMapper.getSubscriptionRecordById(1L)).willReturn(activeRecord(4L));

        subscriptionPaymentService.cancelSubscription(1L, 4L, 4L);

        verify(subscriptionPaymentMapper).cancelSubscription(1L);
    }

    @Test
    @DisplayName("다른 회원의 구독은 해지할 수 없다")
    void cancelSubscription_denied() {
        given(subscriptionPaymentMapper.getSubscriptionRecordById(1L)).willReturn(activeRecord(4L));

        assertThatThrownBy(() -> subscriptionPaymentService.cancelSubscription(1L, 9L, 9L))
                .isInstanceOf(AccessDeniedException.class);

        verify(subscriptionPaymentMapper, never()).cancelSubscription(any());
    }

    @Test
    @DisplayName("본인 구독 목록만 조회한다")
    void getMySubscriptionRecords() {
        SubscriptionRecord record = activeRecord(4L);
        record.setStartedAt(LocalDate.of(2026, 10, 1));
        record.setNextBillingAt(LocalDate.of(2026, 11, 1));
        given(subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(4L)).willReturn(List.of(record));
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());
        given(memberMapper.findById(2L)).willReturn(Member.builder().id(2L).nickname("코코").build());

        assertThat(subscriptionPaymentService.getMySubscriptionRecords(4L, 4L))
                .containsExactly(new MySubscriptionsResponse(
                        1L, 4L, "코코", "베이직", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1), true));

        assertThatThrownBy(() -> subscriptionPaymentService.getMySubscriptionRecords(4L, 9L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("정기결제 성공이면 다음 결제일을 한 달 뒤로 옮긴다")
    void billDueSubscriptions_paid() {
        SubscriptionRecord due = activeRecord(4L);
        due.setPlanId(1L);
        due.setTargetMemberId(2L);
        due.setBillingKey("bk_1");
        given(subscriptionPaymentMapper.getAllDueSubscriptionRecords()).willReturn(List.of(due));
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());
        given(paymentService.preparePayment(any(), any())).willReturn(prepared());
        stubBillingKeyPayment();
        given(paymentService.verifyAndCompletePayment(any(), any()))
                .willReturn(new PaymentCompleteResponse("ORD_1", "PAID", 4900, "성공"));

        subscriptionPaymentService.billDueSubscriptions();

        verify(subscriptionPaymentMapper).renewNextBillingAt(1L, LocalDate.now().plusMonths(1));
        verify(subscriptionPaymentMapper, never()).cancelSubscription(1L);
    }

    @Test
    @DisplayName("정기결제가 FAILED이면 구독을 해지한다")
    void billDueSubscriptions_failed() {
        SubscriptionRecord due = activeRecord(4L);
        due.setPlanId(1L);
        given(subscriptionPaymentMapper.getAllDueSubscriptionRecords()).willReturn(List.of(due));
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());
        given(paymentService.preparePayment(any(), any())).willReturn(prepared());
        stubBillingKeyPayment();
        given(paymentService.verifyAndCompletePayment(any(), any()))
                .willReturn(new PaymentCompleteResponse("ORD_1", "FAILED", null, "실패"));

        subscriptionPaymentService.billDueSubscriptions();

        verify(subscriptionPaymentMapper).cancelSubscription(1L);
        verify(subscriptionPaymentMapper, never()).renewNextBillingAt(any(), any());
    }

    @Test
    @DisplayName("판매가 끝난 플랜의 정기결제 대상은 결제 없이 해지한다")
    void billDueSubscriptions_deletedPlan() {
        SubscriptionRecord due = activeRecord(4L);
        due.setPlanId(1L);
        Subscription deleted = activePlan();
        deleted.setStatus("DELETED");
        given(subscriptionPaymentMapper.getAllDueSubscriptionRecords()).willReturn(List.of(due));
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(deleted);

        subscriptionPaymentService.billDueSubscriptions();

        verify(subscriptionPaymentMapper).cancelSubscription(1L);
        verify(paymentService, never()).preparePayment(any(), any());
    }

    @Test
    @DisplayName("정기결제 중 예외가 나도 그 구독만 해지하고 다음 구독을 계속한다")
    void billDueSubscriptions_exceptionCancelsAndContinues() {
        SubscriptionRecord failed = activeRecord(4L);
        failed.setId(1L);
        failed.setPlanId(1L);
        SubscriptionRecord paid = activeRecord(3L);
        paid.setId(2L);
        paid.setPlanId(1L);
        paid.setBillingKey("bk_1");
        given(subscriptionPaymentMapper.getAllDueSubscriptionRecords()).willReturn(List.of(failed, paid));
        given(subscriptionMapper.getSubscriptionById(1L)).willReturn(activePlan());
        given(paymentService.preparePayment(eq(4L), any())).willThrow(new IllegalStateException("포트원 오류"));
        given(paymentService.preparePayment(eq(3L), any())).willReturn(prepared());
        stubBillingKeyPayment();
        given(paymentService.verifyAndCompletePayment(any(), any()))
                .willReturn(new PaymentCompleteResponse("ORD_1", "PAID", 4900, "성공"));

        subscriptionPaymentService.billDueSubscriptions();

        verify(subscriptionPaymentMapper).cancelSubscription(1L);
        verify(subscriptionPaymentMapper).renewNextBillingAt(2L, LocalDate.now().plusMonths(1));
    }

    private Subscription activePlan() {
        return Subscription.builder()
                .id(1L)
                .memberId(2L)
                .planName("베이직")
                .price(4900)
                .description("월간 전용 피드")
                .status("ACTIVE")
                .build();
    }

    private SubscriptionRecordCreateRequest createRequest() {
        return SubscriptionRecordCreateRequest.builder()
                .targetMemberId(2L)
                .planId(1L)
                .billingKey("bk_1")
                .build();
    }

    private SubscriptionRecord activeRecord(Long memberId) {
        return SubscriptionRecord.builder()
                .id(1L)
                .memberId(memberId)
                .targetMemberId(2L)
                .planId(1L)
                .billingKey("bk_1")
                .status("ACTIVE")
                .agreement(true)
                .build();
    }

    private BillingKey issuedKey(String billingKey) {
        return BillingKey.builder().status("ISSUED").billingKey(billingKey).build();
    }

    private PaymentPrepareResponse prepared() {
        return new PaymentPrepareResponse("ORD_1", "베이직", 4900, "KRW");
    }

    @SuppressWarnings("unchecked")
    private void stubBillingKeyLookup(BillingKey billingKey) {
        doReturn(getSpec).when(restClient).get();
        doReturn(getSpec).when(getSpec).uri(anyString(), any(Object[].class));
        doReturn(getSpec).when(getSpec).header(anyString(), any(String[].class));
        doReturn(getResponse).when(getSpec).retrieve();
        given(getResponse.body(BillingKey.class)).willReturn(billingKey);
    }

    @SuppressWarnings("unchecked")
    private void stubBillingKeyPayment() {
        doReturn(postSpec).when(restClient).post();
        doReturn(postSpec).when(postSpec).uri(anyString(), any(Object[].class));
        doReturn(postSpec).when(postSpec).header(anyString(), any(String[].class));
        doReturn(postSpec).when(postSpec).contentType(any());
        doReturn(postSpec).when(postSpec).body(any(Object.class));
        doReturn(postResponse).when(postSpec).retrieve();
        given(postResponse.body(any(Class.class))).willReturn(null);
    }
}
