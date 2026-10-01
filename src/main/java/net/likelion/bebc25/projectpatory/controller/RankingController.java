package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.RankingSliceResponse;
import net.likelion.bebc25.projectpatory.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    /**
     * 인기펫 랭킹 조회 (팔로워 수 DESC 키셋 슬라이스)
     * <p>
     * 메인피드 TOP 5: GET /api/v1/ranking?size=5
     * 랭킹 페이지:   GET /api/v1/ranking?size=20
     * 다음 페이지:   GET /api/v1/ranking?size=20&amp;lastFollowerCount=100&amp;lastMemberId=3
     */
    @Operation(summary = "인기펫 랭킹", description = "팔로워 수 많은 순으로 조회합니다. 메인피드는 size=5, 랭킹 페이지는 size=20을 사용합니다.")
    @GetMapping
    public ResponseEntity<RankingSliceResponse> getRanking(
            @Parameter(description = "직전에 받은 마지막 팔로워 수 (첫 조회 시 생략)", example = "100")
            @RequestParam(required = false) Long lastFollowerCount,
            @Parameter(description = "직전에 받은 마지막 회원 ID (첫 조회 시 생략)", example = "3")
            @RequestParam(required = false) Long lastMemberId,
            @Parameter(description = "한 번에 조회할 개수 (메인 5 / 랭킹 20)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(rankingService.getRanking(lastFollowerCount, lastMemberId, size));
    }
}
