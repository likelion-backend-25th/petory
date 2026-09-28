package net.likelion.bebc25.projectpatory.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "본인 프로필 조회 응답 (민감 정보 포함)")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MyProfile implements Profile {
    @Schema(description = "회원 ID", example = "1")
    Long id;
    @Schema(description = "회원 이메일", example = "me@petory.com")
    String email;
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
    @Schema(description = "회원 상태", example = "ACTIVE")
    String status;
    @Schema(description = "회원 권한", example = "ROLE_USER")
    String role;
    @Schema(description = "가입 일시", example = "2024-01-01T12:00:00")
    LocalDateTime createdAt;
    @Schema(description = "개인정보 제공 동의 시각", example = "2024-01-01T12:05:00", nullable = true)
    LocalDateTime infoProvideAgreement;
    @Schema(description = "작성 게시글 수", example = "10")
    long postsCount;
    @Schema(description = "팔로워 수", example = "3")
    long followers;
    @Schema(description = "팔로잉 수", example = "5")
    long followings;
}
