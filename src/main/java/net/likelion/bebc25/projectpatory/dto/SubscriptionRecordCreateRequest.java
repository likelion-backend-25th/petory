package net.likelion.bebc25.projectpatory.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionRecordCreateRequest {
    Long targetMemberId;
    Long planId;
    String billingKey;
}
