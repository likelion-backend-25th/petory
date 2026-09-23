package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(description = "로그인 또는 이전 갱신 시 발급받은 refreshToken")
        @NotBlank(message = "Refresh Token은 필수입니다.")
        String refreshToken
) {}
