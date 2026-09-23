package net.likelion.bebc25.projectpatory.dto;

import net.likelion.bebc25.projectpatory.domain.Member;

import java.time.LocalDateTime;

public record MemberProfileResponse(
        Long id,
        String email,
        String nickname,
        String species,
        String sex,
        String birthDate,
        String intro,
        String profileImage,
        String address,
        String status,
        String role,
        LocalDateTime createdAt,
        LocalDateTime infoProvideAgreement
) {
    public static MemberProfileResponse from(Member member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getEmail(),
                member.getNickname(),
                member.getSpecies(),
                member.getSex(),
                member.getBirthDate(),
                member.getIntro(),
                member.getProfileImage(),
                member.getAddress(),
                member.getStatus(),
                member.getRole(),
                member.getCreatedAt(),
                member.getInfoProvideAgreement()
        );
    }
}
