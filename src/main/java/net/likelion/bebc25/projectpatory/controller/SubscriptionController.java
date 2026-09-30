package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.dto.ApiErrorResponse;
import net.likelion.bebc25.projectpatory.dto.SubscriptionCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionUpdateRequest;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Subscription Plan API", description = "구독 플랜 생성, 조회, 수정, 삭제를 담당하는 REST 컨트롤러")
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/{memberId}/new")
    @Operation(
            summary = "구독 플랜 생성",
            description = "로그인한 회원의 구독 플랜을 등록한다. 경로의 회원 ID, 요청 본문의 memberId, 로그인 회원 ID가 모두 같아야 한다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "구독 플랜 생성 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "타인 명의로 구독 플랜 생성 시도",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<Void> createSubscription(
            @Parameter(description = "플랜을 등록할 회원 ID (로그인 회원 ID와 동일해야 함)", example = "2")
            @PathVariable Long memberId,
            @RequestBody SubscriptionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        subscriptionService.createSubscriptionPlan(request, loginUser.getId(), memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{memberId}")
    @Operation(
            summary = "회원별 구독 플랜 목록 조회",
            description = "해당 회원이 등록한 구독 플랜 목록을 조회한다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 플랜 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Subscription.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<List<Subscription>> getSubscriptions(
            @Parameter(description = "플랜을 조회할 회원 ID", example = "2")
            @PathVariable Long memberId
    ) {
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsByMemberId(memberId);
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping("/{memberId}/edit")
    @Operation(
            summary = "구독 플랜 수정",
            description = "본인 구독 플랜의 이름, 설명, 상태를 수정한다. 가격은 변경되지 않는다. 요청 본문의 id로 대상 플랜을 지정한다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "구독 플랜 수정 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "타인 구독 플랜 수정 시도",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 구독 플랜",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<Void> editSubscription(
            @Parameter(description = "플랜 소유 회원 ID (로그인 회원 ID와 동일해야 함)", example = "2")
            @PathVariable("memberId") Long memberId,
            @RequestBody SubscriptionUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        subscriptionService.updateSubscriptionPlan(request, loginUser.getId(), memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{memberId}/delete")
    @Operation(
            summary = "구독 플랜 삭제",
            description = "본인 구독 플랜을 삭제한다. 쿼리 파라미터 id로 대상 플랜을 지정한다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "구독 플랜 삭제 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "타인 구독 플랜 삭제 시도",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 구독 플랜",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<Void> deleteSubscription(
            @Parameter(description = "삭제할 구독 플랜 ID", example = "1")
            @RequestParam Long id,
            @Parameter(description = "플랜 소유 회원 ID (로그인 회원 ID와 동일해야 함)", example = "2")
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        subscriptionService.deleteSubscriptionById(id, loginUser.getId(), memberId);
        return ResponseEntity.ok().build();
    }
}
