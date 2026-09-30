package net.likelion.bebc25.projectpatory.domain;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class SocialAccount {
    Long memberId;
    String provider;
    String providerUserId;
    String providerEmail;
}
