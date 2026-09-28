package net.likelion.bebc25.projectpatory.dto;

import net.likelion.bebc25.projectpatory.domain.Member;

import java.time.LocalDateTime;

public record SignUpResponse(
        Long id,
        String email,
        String status,
        String role,
        LocalDateTime createdAt,
        LocalDateTime infoProvideAgreement
) {
    public static SignUpResponse from(Member member) {
        return new SignUpResponse(
                member.getId(),
                member.getEmail(),
                member.getStatus(),
                member.getRole(),
                member.getCreatedAt(),
                member.getInfoProvideAgreement()
        );
    }
}
