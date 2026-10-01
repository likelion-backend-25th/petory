package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.domain.BillingKey;
import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.domain.SubscriptionRecord;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.mapper.SubscriptionMapper;
import net.likelion.bebc25.projectpatory.mapper.SubscriptionPaymentMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SubscriptionPaymentImpl implements SubscriptionPaymentService {
    private final SubscriptionPaymentMapper subscriptionPaymentMapper;
    private final SubscriptionMapper subscriptionMapper;
    private final PaymentService paymentService;
    private final RestClient restClient;

    @Value("${portone.api.secret}")
    private String apiSecret;
    @Value("${portone.store-id}")
    private String storeId;
    @Value("${portone.channel-key}")
    private String channelKey;

    @Override
    @Transactional
    public void createSubscriptionRecord(
            Long memberId,
            SubscriptionRecordCreateRequest request,
            Long loginMemberId
    ) {
        Subscription subscription = subscriptionMapper.getSubscriptionById(request.getPlanId());
        if (subscription == null
                || subscription.getStatus().equals("DELETED")
                || subscription.getStatus().equals("PENDING_DELETION")) {
            throw new NoSuchElementException("존재하지 않는 구독상품입니다.");
        }
        if (loginMemberId.equals(memberId)) {
            throw new IllegalArgumentException("본인의 구독상품을 구독할 수 없습니다.");
        }
        if (!request.getTargetMemberId().equals(memberId) || !request.getTargetMemberId().equals(subscription.getMemberId())) {
            throw new IllegalArgumentException("결제 진행 중 오류가 발생하였습니다.");
        }

        List<SubscriptionRecord> records = subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(loginMemberId);
        for (SubscriptionRecord record : records) {
            if (record.getPlanId().equals(request.getPlanId())) {
                throw new IllegalArgumentException("이미 구독한 상품입니다.");
            }
        }
        BillingKey billingKey = restClient.get()
                .uri("https://api.portone.io/billing-keys/{billingKey}", request.getBillingKey())
                .header("Authorization", "PortOne " + apiSecret)
                .retrieve()
                .body(BillingKey.class);

        if (billingKey == null
                || !billingKey.getStatus().equals("ISSUED")
                || !billingKey.getBillingKey().equals(request.getBillingKey())) {
            throw new IllegalArgumentException("유효하지 않은 결제수단입니다.");
        }

        PaymentPrepareRequest paymentPrepareRequest = PaymentPrepareRequest.builder()
                .targetMemberId(request.getTargetMemberId())
                .orderName(subscription.getPlanName())
                .totalAmount(subscription.getPrice())
                .merchandise("automaticPayment")
                .build();
        PaymentPrepareResponse prepareResponse = paymentService.preparePayment(loginMemberId, paymentPrepareRequest);

        PortOneBillingKeyPaymentResponse response = restClient.post()
                .uri("https://api.portone.io/payments/{paymentId}/billing-key", prepareResponse.paymentId())
                .header("Authorization", "PortOne " + apiSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PortOneBillingKeyPaymentRequest(
                        request.getBillingKey(),
                        subscription.getPlanName(),
                        new PortOneBillingKeyPaymentRequest.Amount(subscription.getPrice()),
                        "KRW"))
                .retrieve()
                .body(PortOneBillingKeyPaymentResponse.class);

        PaymentCompleteRequest paymentCompleteRequest = new PaymentCompleteRequest(prepareResponse.paymentId());
        PaymentCompleteResponse paymentCompleteResponse = paymentService.verifyAndCompletePayment(loginMemberId, paymentCompleteRequest);

        if (!paymentCompleteResponse.status().equals("PAID")) {
            throw new IllegalArgumentException("결제 진행 도중 오류가 발생하였습니다.");
        }
        LocalDate nextBillingAt = LocalDate.now().plusMonths(1);
        subscriptionPaymentMapper.createSubscriptionRecord(loginMemberId, request, nextBillingAt);
    }

    @Override
    public void updateSubscriptionRecord(SubscriptionRecordUpdateRequest request, Long loginMemberId) {
        SubscriptionRecord record = subscriptionPaymentMapper.getSubscriptionRecordById(request.getId());
        if (!record.getMemberId().equals(loginMemberId)) {
            throw new AccessDeniedException("비정상적인 접근입니다.");
        }
        if (record.getStatus().equals("CANCELLED")) {
            throw new NoSuchElementException("이미 구독 해지한 상품입니다.");
        }
        subscriptionPaymentMapper.updateSubscriptionRecord(request);
    }

    @Override
    public void cancelSubscription(Long subscriptionId, Long loginMemberId) {
        SubscriptionRecord record = subscriptionPaymentMapper.getSubscriptionRecordById(subscriptionId);
        if (!record.getMemberId().equals(loginMemberId)) {
            throw new AccessDeniedException("비정상적인 접근입니다.");
        }
        if (record.getStatus().equals("CANCELLED")) {
            throw new NoSuchElementException("이미 구독 해지한 상품입니다.");
        }
        subscriptionPaymentMapper.cancelSubscription(subscriptionId);
    }

    @Override
    public SubscriptionRecord getSubscriptionRecord(Long subscriptionId) {
        return subscriptionPaymentMapper.getSubscriptionRecordById(subscriptionId);
    }

    @Override
    public List<SubscriptionRecord> getMySubscriptionRecords(Long memberId, Long loginMemberId) {
        if (!memberId.equals(loginMemberId)) {
            throw new AccessDeniedException("비정상적인 접근입니다.");
        }
        return subscriptionPaymentMapper.getSubscriptionRecordsByMemberId(memberId);
    }


    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void billDueSubscriptions() {
        List<SubscriptionRecord> records = subscriptionPaymentMapper.getAllDueSubscriptionRecords();
        for (SubscriptionRecord record : records) {
            try {
                billingKeyPayment(record);
            } catch (Exception e) {
                subscriptionPaymentMapper.cancelSubscription(record.getId());
            }
        }
    }

    private void billingKeyPayment(SubscriptionRecord record) {
        Subscription subscription = subscriptionMapper.getSubscriptionById(record.getPlanId());

        if (subscription == null
                || subscription.getStatus().equals("DELETED")
                || subscription.getStatus().equals("PENDING_DELETION")) {
            subscriptionPaymentMapper.cancelSubscription(record.getId());
            return;
        }

        PaymentPrepareRequest paymentPrepareRequest = PaymentPrepareRequest.builder()
                .targetMemberId(record.getTargetMemberId())
                .orderName(subscription.getPlanName())
                .totalAmount(subscription.getPrice())
                .merchandise("automaticPayment")
                .build();
        PaymentPrepareResponse prepareResponse = paymentService.preparePayment(record.getMemberId(), paymentPrepareRequest);

        PortOneBillingKeyPaymentResponse response = restClient.post()
                .uri("https://api.portone.io/payments/{paymentId}/billing-key", prepareResponse.paymentId())
                .header("Authorization", "PortOne " + apiSecret)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new PortOneBillingKeyPaymentRequest(
                        record.getBillingKey(),
                        subscription.getPlanName(),
                        new PortOneBillingKeyPaymentRequest.Amount(subscription.getPrice()),
                        "KRW"))
                .retrieve()
                .body(PortOneBillingKeyPaymentResponse.class);

        PaymentCompleteRequest paymentCompleteRequest = new PaymentCompleteRequest(prepareResponse.paymentId());
        PaymentCompleteResponse paymentCompleteResponse = paymentService.verifyAndCompletePayment(record.getMemberId(), paymentCompleteRequest);

        if (paymentCompleteResponse.status().equals("PAID")) {
            LocalDate nextBillingAt = LocalDate.now().plusMonths(1);
            subscriptionPaymentMapper.renewNextBillingAt(record.getId(), nextBillingAt);
        }
        else if (paymentCompleteResponse.status().equals("FAILED")) {
            subscriptionPaymentMapper.cancelSubscription(record.getId());
        }
    }

}
