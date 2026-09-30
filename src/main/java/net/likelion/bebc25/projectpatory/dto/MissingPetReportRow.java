package net.likelion.bebc25.projectpatory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MissingPetReportRow(
        Long id,

        Long reporterId,
        String reporterNickname,
        String reporterProfileImage,

        String address,
        String detail,
        String imageUrl,

        LocalDateTime sightAt,

        BigDecimal latitude,
        BigDecimal longitude,

        LocalDateTime createdAt
) {
}