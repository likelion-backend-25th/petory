package net.likelion.bebc25.projectpatory.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = "프로필 조회 응답. 본인 조회 시 MyProfileResponse, 타인 조회 시 MemberProfileResponse",
        oneOf = {MyProfile.class, MemberProfile.class}
)
public interface Profile {}
