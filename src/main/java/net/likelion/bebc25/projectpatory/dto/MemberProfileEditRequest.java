package net.likelion.bebc25.projectpatory.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MemberProfileEditRequest {
    String nickname;
    String species;
    String sex;
    String birthday;
    String intro;
    String profileImage;
    String address;
}
