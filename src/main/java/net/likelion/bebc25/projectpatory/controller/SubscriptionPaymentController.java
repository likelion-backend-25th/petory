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
import net.likelion.bebc25.projectpatory.dto.ApiErrorResponse;
import net.likelion.bebc25.projectpatory.dto.MySubscriptionsResponse;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionRecordUpdateRequest;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.SubscriptionPaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Subscription Payment API", description = "구독 결제, 조회, 동의 변경, 해지를 담당하는 REST 컨트롤러")
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionPaymentController {
    private final SubscriptionPaymentService subscriptionPaymentService;

    @PostMapping("/{memberId}")
    @Operation(
            summary = "구독 첫 결제",
            description = "프론트가 발급받은 빌링키로 첫 결제를 진행하고, 결제 성공 시 구독을 등록한다. 경로의 회원 ID는 플랜 소유자이며 요청의 targetMemberId, 플랜 소유 회원 ID와 같아야 한다. 로그인 회원은 구독자이며 본인 플랜은 구독할 수 없다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "구독 결제 및 등록 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "본인 플랜 구독, 결제 대상 불일치, 중복 구독, 유효하지 않은 빌링키, 결제 실패",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 삭제된 구독 플랜",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<Void> createSubscriptionRecord(
            @Parameter(description = "구독할 플랜의 소유 회원 ID", example = "2")
            @PathVariable Long memberId,
            @RequestBody SubscriptionRecordCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        subscriptionPaymentService.createSubscriptionRecord(memberId, request, loginMember.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{memberId}")
    @Operation(
            summary = "내 구독 목록 조회",
            description = "로그인 회원의 활성 구독 목록을 조회한다. 경로의 회원 ID는 로그인 회원 ID와 같아야 한다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 목록 조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MySubscriptionsResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "타인 구독 목록 조회 시도",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<List<MySubscriptionsResponse>> getMySubscriptionRecords(
            @Parameter(description = "구독 목록을 조회할 회원 ID (로그인 회원 ID와 동일해야 함)", example = "4")
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        return ResponseEntity.ok(subscriptionPaymentService.getMySubscriptionRecords(memberId, loginMember.getId()));
    }

    @GetMapping("/{memberId}/{subscriptionRecordId}")
    @Operation(
            summary = "내 구독 단건 조회",
            description = "로그인 회원의 활성 구독 한 건을 조회한다. 경로의 회원 ID는 로그인 회원 ID, 구독의 구독자 ID와 같아야 한다. 해지된 구독은 조회되지 않는다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "구독 단건 조회 성공",
                    content = @Content(schema = @Schema(implementation = MySubscriptionsResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "타인 구독 조회 시도",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 이미 해지된 구독",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<MySubscriptionsResponse> getSubscriptionRecord(
            @Parameter(description = "구독자 회원 ID (로그인 회원 ID와 동일해야 함)", example = "4")
            @PathVariable("memberId") Long memberId,
            @Parameter(description = "조회할 구독 ID", example = "1")
            @PathVariable("subscriptionRecordId") Long subscriptionRecordId,
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        return ResponseEntity.ok(subscriptionPaymentService.getSubscriptionRecord(subscriptionRecordId, memberId, loginMember.getId()));
    }

    @PatchMapping("/{memberId}/{subscriptionRecordId}")
    @Operation(
            summary = "다음 달 구독 유지 동의 변경",
            description = "본인 구독의 다음 달 유지 동의 여부를 변경한다. 요청 본문의 id는 경로의 구독 ID와 같아야 하고, 경로의 회원 ID는 로그인 회원 ID와 같아야 한다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "구독 유지 동의 변경 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "본문 id와 경로의 구독 ID 불일치, 또는 타인 구독 변경 시도",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 이미 해지된 구독",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<Void> updateSubscriptionRecord(
            @Parameter(description = "구독자 회원 ID (로그인 회원 ID와 동일해야 함)", example = "4")
            @PathVariable("memberId") Long memberId,
            @Parameter(description = "동의 여부를 변경할 구독 ID", example = "1")
            @PathVariable("subscriptionRecordId") Long subscriptionRecordId,
            @RequestBody SubscriptionRecordUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        subscriptionPaymentService.updateSubscriptionRecord(request, loginMember.getId(), memberId, subscriptionRecordId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{memberId}/{subscriptionRecordId}")
    @Operation(
            summary = "구독 해지",
            description = "본인 구독을 해지한다. 구독 행은 삭제하지 않고 상태를 CANCELLED로 바꾼다. 경로의 회원 ID는 로그인 회원 ID와 같아야 한다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "구독 해지 성공"),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "타인 구독 해지 시도",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않거나 이미 해지된 구독",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    ResponseEntity<Void> cancelSubscription(
            @Parameter(description = "구독자 회원 ID (로그인 회원 ID와 동일해야 함)", example = "4")
            @PathVariable("memberId") Long memberId,
            @Parameter(description = "해지할 구독 ID", example = "1")
            @PathVariable("subscriptionRecordId") Long subscriptionRecordId,
            @AuthenticationPrincipal CustomUserDetails loginMember
    ) {
        subscriptionPaymentService.cancelSubscription(subscriptionRecordId, loginMember.getId(), memberId);
        return ResponseEntity.ok().build();
    }
}
