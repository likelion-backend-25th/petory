package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record PostResponse(
        @Schema(description = "게시글 고유 식별자(PK)", example = "1")
        Long id,

        @Schema(description = "작성자 회원 ID", example = "10")
        Long memberId,

        @Schema(description = "게시글 본문 내용", example = "스프링 부트 REST API 학습 중입니다.")
        String content,

        @Schema(description = "첨부 이미지 URL", example = "https://example.com/images/post1.png", nullable = true)
        String imageUrl,

        @Schema(description = "좋아요 누적 수", example = "89")
        int likeCount,

        @Schema(description = "게시글 등록 일시", example = "2026-08-07T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "게시글 최종 수정 일시", example = "2026-08-07T10:30:00")
        LocalDateTime updatedAt
) {
        // 신규 게시글 등록 요청 DTO로 부터 게시글 응답 DTD를 생성하는 팩토리 메서드

        }
