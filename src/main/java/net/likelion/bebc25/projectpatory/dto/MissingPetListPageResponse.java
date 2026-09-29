package net.likelion.bebc25.projectpatory.dto;

import java.util.List;

public record MissingPetListPageResponse(
        Long totalCount,
        List<MissingPetListResponse> items,
        Long nextCursor,
        boolean hasNext
) {
}