package net.likelion.bebc25.projectpatory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MissingPetUpdateRequest(
        LocalDate missingDate,
        String missingAddress,
        String detail,
        String imageUrl,
        BigDecimal latitude,
        BigDecimal longitude
) {
}