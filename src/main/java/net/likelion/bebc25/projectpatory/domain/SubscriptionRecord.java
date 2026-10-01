package net.likelion.bebc25.projectpatory.domain;

import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionRecord {
    Long id;
    Long memberId;
    Long targetMemberId;
    Long planId;
    String billingKey;
    LocalDate startedAt;
    LocalDate endedAt;
    LocalDate nextBillingAt;
    String status;
    boolean agreement;
}
