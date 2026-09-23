package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginRequest(
        @Schema(description = "회원 email", example = "user1@example.com")
        String email,
        @Schema(description = "회원 비밀번호", example = "password123")
        String password
) {}
