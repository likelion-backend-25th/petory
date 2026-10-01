package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.MySubscriptionsResponse;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordUpdateRequest;

import java.util.List;

public interface SubscriptionPaymentService {
    void createSubscriptionRecord(
            Long memberId,
            SubscriptionRecordCreateRequest request,
            Long loginMemberId
    );

    void updateSubscriptionRecord(SubscriptionRecordUpdateRequest request, Long loginMemberId, Long memberId, Long subscriptionId);

    void cancelSubscription(Long subscriptionId, Long memberId, Long loginMemberId);

    MySubscriptionsResponse getSubscriptionRecord(Long subscriptionId, Long memberId, Long loginMemberId);

    List<MySubscriptionsResponse> getMySubscriptionRecords(Long memberId, Long loginMemberId);
}
