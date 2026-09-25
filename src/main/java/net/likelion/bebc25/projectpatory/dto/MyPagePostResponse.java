package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "마이페이지 게시글/북마크 목록 항목")
public record MyPagePostResponse(
        @Schema(description = "게시글 ID", example = "10")
        Long id,
        @Schema(description = "게시글 본문", example = "오늘 산책했어요")
        String content,
        @Schema(description = "구독자 전용 여부", example = "false")
        boolean isSubscriberOnly,
        @Schema(description = "해시태그", example = "#산책 #일상")
        String hashtags,
        @Schema(description = "대표 이미지 URL", example = "https://example.com/img1.png", nullable = true)
        String imageUrl,
        @Schema(description = "좋아요 수", example = "3")
        int likeCount
) {}
