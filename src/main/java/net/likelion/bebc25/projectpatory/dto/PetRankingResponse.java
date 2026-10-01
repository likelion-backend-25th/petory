package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "인기펫 랭킹 항목")
public class PetRankingResponse {

    @Schema(description = "회원 ID", example = "3")
    private Long memberId;

    @Schema(description = "닉네임", example = "뭉이")
    private String nickname;

    @Schema(description = "프로필 이미지 URL")
    private String profileImage;

    @Schema(description = "팔로워 수", example = "14")
    private long followerCount;
}
