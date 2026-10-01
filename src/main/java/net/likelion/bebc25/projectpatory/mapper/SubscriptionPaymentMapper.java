package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.SubscriptionRecord;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SubscriptionPaymentMapper {
    void createSubscriptionRecord(
            Long memberId,
            @Param("request") SubscriptionRecordCreateRequest request,
            LocalDate nextBillingAt
    );

    void updateSubscriptionRecord(@Param("request") SubscriptionRecordUpdateRequest request);

    void cancelSubscription(Long id);

    SubscriptionRecord getSubscriptionRecordById(@Param("id") Long id);

    List<SubscriptionRecord> getSubscriptionRecordsByMemberId(@Param("memberId") Long memberId);

    List<SubscriptionRecord> getAllDueSubscriptionRecords();

    void renewNextBillingAt(Long id, LocalDate nextBillingAt);
}
