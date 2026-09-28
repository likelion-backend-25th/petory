package net.likelion.bebc25.projectpatory.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Schema(description = "타인 프로필 조회 응답 (이메일·주소 등 민감 정보 제외)")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class MemberProfile implements Profile {
    @Schema(description = "회원 ID", example = "2")
    Long id;
    @Schema(description = "반려동물 닉네임", example = "별이")
    String nickname;
    @Schema(description = "자기소개", example = "안녕")
    String intro;
    @Schema(description = "프로필 이미지 URL", example = "https://example.com/other.png")
    String profileImage;
    @Schema(description = "회원 상태", example = "ACTIVE")
    String status;
    @Schema(description = "회원 권한", example = "ROLE_USER")
    String role;
    @Schema(description = "가입 일시", example = "2024-02-01T10:00:00")
    LocalDateTime createdAt;
    @Schema(description = "작성 게시글 수", example = "4")
    long postsCount;
    @Schema(description = "팔로워 수", example = "1")
    long followers;
    @Schema(description = "팔로잉 수", example = "2")
    long followings;
}
