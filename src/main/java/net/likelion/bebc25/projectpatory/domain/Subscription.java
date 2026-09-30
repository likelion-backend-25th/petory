package net.likelion.bebc25.projectpatory.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Subscription {
    Long id;
    Long memberId;
    String planName;
    int price;
    String description;
    String status;
}
