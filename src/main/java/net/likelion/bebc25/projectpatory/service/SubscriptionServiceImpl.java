package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.dto.SubscriptionCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionUpdateRequest;
import net.likelion.bebc25.projectpatory.mapper.SubscriptionMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionMapper subscriptionMapper;

    public SubscriptionServiceImpl(SubscriptionMapper subscriptionMapper) {
        this.subscriptionMapper = subscriptionMapper;
    }

    @Override
    public void createSubscriptionPlan(SubscriptionCreateRequest request, Long loginMemberId, Long memberId) {
        if (!loginMemberId.equals(memberId) || !loginMemberId.equals(request.getMemberId())) {
            throw new AccessDeniedException("비정상적인 접근입니다.");
        }
        subscriptionMapper.createSubscription(request);
    }

    @Override
    public Subscription getSubscriptionById(Long id) {
        return subscriptionMapper.getSubscriptionById(id);
    }

    @Override
    public List<Subscription> getSubscriptionsByMemberId(Long memberId) {
        return subscriptionMapper.getSubscriptionsByMemberId(memberId);
    }

    @Override
    public void updateSubscriptionPlan(SubscriptionUpdateRequest request, Long loginMemberId, Long memberId) {
        Subscription subscription = subscriptionMapper.getSubscriptionById(request.getId());
        if (subscription == null) {
            throw new NoSuchElementException("더이상 존재하지 않는 구독플랜입니다.");
        }
        if (!loginMemberId.equals(memberId) || !subscription.getMemberId().equals(loginMemberId)) {
            throw new AccessDeniedException("비정상적인 접근입니다.");
        }
        subscriptionMapper.updateSubscription(request);
    }

    @Override
    public void deleteSubscriptionById(Long id, Long loginMemberId, Long memberId) {
        Subscription subscription = subscriptionMapper.getSubscriptionById(id);
        if (subscription == null) {
            throw new NoSuchElementException("더이상 존재하지 않는 구독플랜입니다.");
        }
        if (!subscription.getMemberId().equals(memberId) || !subscription.getMemberId().equals(loginMemberId)) {
            throw new AccessDeniedException("비정상적인 접근입니다.");
        }
        subscriptionMapper.deleteSubscriptionById(id);
    }
}
