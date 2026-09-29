package net.likelion.bebc25.projectpatory.controller;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.MissingPetListPageResponse;
import net.likelion.bebc25.projectpatory.service.MissingPetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/missing-pets")
public class MissingPetController {

    private final MissingPetService missingPetService;

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
}