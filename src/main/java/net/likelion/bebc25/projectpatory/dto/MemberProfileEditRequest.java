package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Schema(description = "내 프로필 수정 요청")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MemberProfileEditRequest {
    @Schema(description = "반려동물 닉네임", example = "뭉치")
    String nickname;

    @Schema(description = "반려동물 종류", example = "개")
    String species;

    @Schema(description = "반려동물 성별", example = "수")
    String sex;

    @Schema(description = "반려동물 생일", example = "2020-05-01")
    LocalDate birthDate;

    @Schema(description = "자기소개", example = "안녕하세요")
    String intro;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/me.png")
    String profileImage;

    @Schema(description = "거주지 주소", example = "서울시 강남구")
    String address;
}
