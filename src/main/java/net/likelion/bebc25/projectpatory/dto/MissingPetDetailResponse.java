package net.likelion.bebc25.projectpatory.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MissingPetDetailResponse(
        Long id,
        MemberSummaryResponse author,
        LocalDate missingDate,
        String missingAddress,
        String detail,
        String imageUrl,
        String status,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MissingPetReportResponse> reports
) {
}