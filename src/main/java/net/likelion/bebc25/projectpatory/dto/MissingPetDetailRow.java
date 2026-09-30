package net.likelion.bebc25.projectpatory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MissingPetDetailRow(
        Long id,

        Long authorId,
        String authorNickname,
        String authorProfileImage,

        LocalDate missingDate,
        String missingAddress,
        String detail,
        String imageUrl,
        String status,

        BigDecimal latitude,
        BigDecimal longitude,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}