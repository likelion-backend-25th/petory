package net.likelion.bebc25.projectpatory.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.domain.Subscription;
import net.likelion.bebc25.projectpatory.dto.SubscriptionCreateRequest;
import net.likelion.bebc25.projectpatory.dto.SubscriptionUpdateRequest;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/{memberId}/new")
    ResponseEntity<Void> createSubscription(
            @PathVariable Long memberId,
            @RequestBody SubscriptionCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        subscriptionService.createSubscriptionPlan(request, loginUser.getId(), memberId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{memberId}")
    ResponseEntity<List<Subscription>> getSubscriptions(
            @PathVariable Long memberId
    ) {
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsByMemberId(memberId);
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping("/{memberId}/edit")
    ResponseEntity<Void> editSubscription(
            @PathVariable("memberId") Long memberId,
            @RequestBody SubscriptionUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        subscriptionService.updateSubscriptionPlan(request, loginUser.getId(), memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{memberId}/delete")
    ResponseEntity<Void> deleteSubscription(
            @RequestParam Long id,
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal CustomUserDetails loginUser
    ) {
        subscriptionService.deleteSubscriptionById(id, loginUser.getId(), memberId);
        return ResponseEntity.ok().build();
    }
}
