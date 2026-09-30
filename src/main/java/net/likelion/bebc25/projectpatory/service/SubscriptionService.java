package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.dto.SubscriptionCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionUpdateRequest;

import java.util.List;

public interface SubscriptionService {
    void createSubscriptionPlan(SubscriptionCreateRequest request, Long loginMemberId, Long memberId);

    Subscription getSubscriptionById(Long id);

    List<Subscription> getSubscriptionsByMemberId(Long memberId);

    void updateSubscriptionPlan(SubscriptionUpdateRequest request, Long loginMemberId, Long memberId);

    void deleteSubscriptionById(Long id, Long loginMemberId, Long memberId);
}
