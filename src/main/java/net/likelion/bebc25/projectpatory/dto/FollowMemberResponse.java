package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "팔로워/팔로잉 목록 항목")
public class FollowMemberResponse {

    @Schema(description = "follow 테이블 PK (커서용)", example = "7")
    private Long followId;

    @Schema(description = "회원 ID", example = "3")
    private Long memberId;

    @Schema(description = "닉네임", example = "나비")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImage;

    @Schema(description = "상대방을 팔로우 하는지의 여부", example = "true")
    private boolean isFollowing;
}
