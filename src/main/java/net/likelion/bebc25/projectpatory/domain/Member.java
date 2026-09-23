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
    private String password;
    private String nickname;
    private String species;
    private String sex;
    private String birthDate;
    @Builder.Default
    private String intro = "안녕하세요";
    @Builder.Default
    private String profileImage = "";
    @Builder.Default
    private String address = "";
    @Builder.Default
    private String status = "ACTIVE";
    @Builder.Default
    private String role = "ROLE_USER";
    private LocalDateTime createdAt;
    @Builder.Default
    private LocalDateTime infoProvideAgreement = null;
}