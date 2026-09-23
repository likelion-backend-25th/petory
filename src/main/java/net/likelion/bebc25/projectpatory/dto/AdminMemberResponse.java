package net.likelion.bebc25.projectpatory.dto;

import net.likelion.bebc25.projectpatory.domain.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminMemberResponse(
        Long id,
        String email,
        String nickname,
        String species,
        String sex,
        LocalDate birthDate,
        String profileImage,
        String address,
        String status,
        String role,
        LocalDateTime createdAt
) {
    public static AdminMemberResponse from(Member member) {
        return new AdminMemberResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getSpecies(),
                member.getSex(),
                member.getBirthDate(),
                member.getProfileImage(),
                member.getAddress(),
                member.getStatus(),
                member.getRole(),
                member.getCreatedAt()
        );
}
