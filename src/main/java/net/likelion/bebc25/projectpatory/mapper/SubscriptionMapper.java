package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.dto.SubscriptionCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubscriptionMapper {
    void createSubscription(@Param("request") SubscriptionCreateRequest request);

    Subscription getSubscriptionById(@Param("id") Long id);

    List<Subscription> getSubscriptionsByMemberId(@Param("memberId") Long memberId);

    void updateSubscription(@Param("request") SubscriptionUpdateRequest request);

    void deleteSubscriptionById(@Param("id") Long id);
}
