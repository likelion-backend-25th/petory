package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.FollowMemberResponse;
import net.likelion.bebc25.projectpatory.dto.FollowToggleResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile/{memberId}")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "팔로우 토글")
    @PostMapping("/follow")
    public ResponseEntity<FollowToggleResponse> toggleFollow(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(followService.toggleFollow(memberId, userDetails.getId()));
    }

    @Operation(summary = "팔로우 취소 (토글과 동일 동작)")
    @DeleteMapping("/follow")
    public ResponseEntity<FollowToggleResponse> unfollow(
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(followService.toggleFollow(memberId, userDetails.getId()));
    }

    @Operation(summary = "팔로워 목록")
    @GetMapping("/followers")
    public ResponseEntity<SliceResponse<FollowMemberResponse>> getFollowers(
            @PathVariable Long memberId,
            @RequestParam(required = false) Long lastFollowId,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                followService.getFollowers(memberId, userDetails.getId(), lastFollowId, size));
    }

    @Operation(summary = "팔로잉 목록")
    @GetMapping("/followings")
    public ResponseEntity<SliceResponse<FollowMemberResponse>> getFollowings(
            @PathVariable Long memberId,
            @RequestParam(required = false) Long lastFollowId,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
                followService.getFollowings(memberId, userDetails.getId(), lastFollowId, size));
    }
}