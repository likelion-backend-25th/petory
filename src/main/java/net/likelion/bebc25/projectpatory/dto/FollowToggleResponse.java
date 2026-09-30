package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "팔로워 토글 응답")
public class FollowToggleResponse {

    @Schema(description = "상대 팔로잉 여부", example = "true")
    private boolean following;

    @Schema(description = "상대 팔로워 수", example = "4")
    private long followerCount;
}
