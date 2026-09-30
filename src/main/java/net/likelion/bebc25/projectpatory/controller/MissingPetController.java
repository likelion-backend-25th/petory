package net.likelion.bebc25.projectpatory.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.MissingPetDetailResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetListPageResponse;
import net.likelion.bebc25.projectpatory.service.MissingPetService;
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
}