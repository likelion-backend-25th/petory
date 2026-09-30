package net.likelion.bebc25.projectpatory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingPetPost {

    private Long id;

    private Long memberId;

    private LocalDate missingDate;

    private String missingAddress;

    private String detail;

    private String imageUrl;

    private MissingPetStatus status;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}