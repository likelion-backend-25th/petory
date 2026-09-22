package net.likelion.bebc25.projectpatory.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Member {
    private Long id;
    private String email;
    private String nickname;
    private String password;
    private String profileImage;
    @Builder.Default
    private String role = "ROLE_USER";
    private LocalDateTime createdAt;
}