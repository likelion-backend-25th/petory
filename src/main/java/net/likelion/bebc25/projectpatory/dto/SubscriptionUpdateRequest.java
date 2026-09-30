package net.likelion.bebc25.projectpatory.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SubscriptionUpdateRequest {
    Long id;
    Long memberId;
    String planName;
    int price;
    String description;
    String status;
}
