package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.SubscriptionRecord;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordUpdateRequest;

import java.util.List;

public interface SubscriptionPaymentService {
    void createSubscriptionRecord(
            Long memberId,
            SubscriptionRecordCreateRequest request,
            Long loginMemberId
    );

    void updateSubscriptionRecord(SubscriptionRecordUpdateRequest request, Long loginMemberId);

    void cancelSubscription(Long subscriptionId, Long loginMemberId);

    SubscriptionRecord getSubscriptionRecord(Long subscriptionId);

    List<SubscriptionRecord> getMySubscriptionRecords(Long memberId, Long loginMemberId);
}
