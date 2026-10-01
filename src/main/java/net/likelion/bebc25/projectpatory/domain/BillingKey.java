package net.likelion.bebc25.projectpatory.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BillingKey {
    String status;
    String billingKey;
}
