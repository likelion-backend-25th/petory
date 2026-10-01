package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.MissingPetService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Missing Pet", description = "실종 동물 신고 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missing-pets")
public class MissingPetController {

    private final MissingPetService missingPetService;

    // 분실동물 목록 조회
    @Operation(
            summary = "실종 동물 목록 조회",
            description = "커서 기반 페이지네이션을 사용하여 실종 동물 게시글 목록을 조회합니다."
    )
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
    @Operation(
            summary = "실종 동물 상세 조회",
            description = "실종 신고 게시글 ID를 기준으로 작성자 정보, 실종 정보, 목격 제보 목록을 조회합니다."
    )
    @GetMapping("/{id}")
    public MissingPetDetailResponse getMissingPetDetail(
            @PathVariable Long id
    ) {
        return missingPetService.getMissingPetDetail(id);
    }

    // 실종 신고
    @Operation(
            summary = "실종 신고 등록",
            description = "현재 로그인한 회원이 실종 동물 신고 게시글을 등록합니다. 최초 상태는 MISSING으로 저장됩니다."
    )
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
    @Operation(
            summary = "실종 신고 수정",
            description = "현재 로그인한 회원이 작성한 실종 신고 게시글의 실종일, 장소, 특이사항, 이미지, 위치 정보를 수정합니다."
    )
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
    @Operation(
            summary = "실종 신고 상태 변경",
            description = "현재 로그인한 작성자가 실종 신고 상태를 FOUND 또는 CANCELLED로 변경합니다."
    )
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