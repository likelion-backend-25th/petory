package net.likelion.bebc25.projectpatory.dto;

import net.likelion.bebc25.projectpatory.domain.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyProfileResponse(
        Long id,
        String email,
        String nickname,
        String species,
        String sex,
        LocalDate birthDate,
        String intro,
        String profileImage,
        String address,
        String status,
        String role,
        LocalDateTime createdAt,
        LocalDateTime infoProvideAgreement,
        long postsCount,
        long followers,
        long followings
) implements ProfileResponse {
    public static MyProfileResponse from(Member member, long postsCount, long followerCount, long followingCount) {
        return new MyProfileResponse(
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
                member.getInfoProvideAgreement(),
                postsCount,
                followerCount,
                followingCount
        );
    }
}
