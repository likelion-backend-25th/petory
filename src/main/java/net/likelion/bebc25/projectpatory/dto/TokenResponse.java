package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "발급된 accessToken")
        String accessToken,
        @Schema(description = "발급된 refreshToken")
        String refreshToken,
        @Schema(description = "토큰 형식", example = "Bearer")
        String tokenType,
        @Schema(description = "토큰 만료시간 (초단위)", example = "3600")
        Long expiresIn
) {
    public static TokenResponse of(String accessToken, String refreshToken, Long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
