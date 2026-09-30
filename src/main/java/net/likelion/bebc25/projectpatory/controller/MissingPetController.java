package net.likelion.bebc25.projectpatory.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.MissingPetService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/missing-pets")
public class MissingPetController {

    private final MissingPetService missingPetService;

    // 분실동물 목록 조회
    @GetMapping
    public MissingPetListPageResponse getMissingPetList(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return missingPetService.getMissingPetList(
                cursor,
                size
        );
    }

    // 분실동물 상세 조회
    @GetMapping("/{id}")
    public MissingPetDetailResponse getMissingPetDetail(
            @PathVariable Long id
    ) {

        return missingPetService.getMissingPetDetail(id);
    }

    // 실종 신고
    @PostMapping
    public Long createMissingPet(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MissingPetCreateRequest request
    ) {
        return missingPetService.createMissingPet(
                userDetails.getId(),
                request
        );
    }

    // 실종 신고 수정
    @PutMapping("/{id}")
    public void updateMissingPet(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MissingPetUpdateRequest request
    ) {

        missingPetService.updateMissingPet(
                userDetails.getId(),
                id,
                request
        );
    }

    // 실종 상태 변경
    @PatchMapping("/{id}/status")
    public void updateMissingPetStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody MissingPetStatusUpdateRequest request
    ) {

        missingPetService.updateMissingPetStatus(
                userDetails.getId(),
                id,
                request
        );
    }
}