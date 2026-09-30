package net.likelion.bebc25.projectpatory.dto;

import net.likelion.bebc25.projectpatory.domain.MissingPetStatus;

public record MissingPetStatusUpdateRequest(
        MissingPetStatus status
) {
}