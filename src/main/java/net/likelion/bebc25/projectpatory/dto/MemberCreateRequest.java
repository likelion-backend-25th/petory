package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class MemberCreateRequest {
    @Schema(description = "회원의 email, 로그인 할 때 식별용으로 사용")
    private String email;

    @Schema(description = "회원의 비밀번호, 로그인 할 때 인증용으로 사용")
    private String password;
}
