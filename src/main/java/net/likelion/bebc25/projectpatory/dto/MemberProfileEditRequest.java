package net.likelion.bebc25.projectpatory.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MemberProfileEditRequest {
    Long memberId;
    String nickname;
    String species;
    String sex;
    String birthDate;
    String intro;
    String profileImage;
    String address;
}
