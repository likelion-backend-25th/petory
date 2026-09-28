package net.likelion.bebc25.projectpatory.domain;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RefreshToken {
    Long id;
    Long memberId;
    String refreshToken;
    LocalDateTime expiration;
}
