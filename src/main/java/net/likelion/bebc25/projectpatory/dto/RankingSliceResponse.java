package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "인기펫 랭킹 슬라이스 응답")
public class RankingSliceResponse {

    @Schema(description = "랭킹 목록")
    private final List<PetRankingResponse> content;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private final boolean hasNext;

    @Schema(description = "다음 요청용 마지막 회원 ID (커서)", example = "3")
    private final Long lastMemberId;

    @Schema(description = "다음 요청용 마지막 팔로워 수 (커서)", example = "20")
    private final Long lastFollowerCount;

    public RankingSliceResponse(List<PetRankingResponse> content, boolean hasNext,
                                Long lastMemberId, Long lastFollowerCount) {
        this.content = content;
        this.hasNext = hasNext;
        this.lastMemberId = lastMemberId;
        this.lastFollowerCount = lastFollowerCount;
    }
}
