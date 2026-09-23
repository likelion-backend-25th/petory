package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 상세 조회 응답 DTO")
public class PostDetailResponse {

    @Schema(description = "게시글 ID", example = "3")
    private Long id;

    @Schema(description = "게시글 본문 내용", example = "오늘 날씨가 좋아서 반려동물과 산책 다녀왔어요!")
    private String content;

    @Schema(description = "배경음악 URL", example = "https://example.com/bgm.mp3")
    private String bgmUrl;

    @Schema(description = "구독자 전용 여부 (0: 전체공개, 1: 구독자전용)", example = "0")
    private Integer isSubscriberOnly;

    @Schema(description = "해시태그 목록", example = "#산책 #반려동물 #일상")
    private String hashtags;

    @Schema(description = "작성자 ID (member_id)", example = "1")
    private Long memberId;

    @Schema(description = "작성자 닉네임", example = "유동근")
    private String authorName;

    @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String authorProfileImage;

    @Schema(description = "게시글 작성 일시", example = "2026-09-23T13:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "게시글 수정 일시", example = "2026-09-23T13:35:00")
    private LocalDateTime updatedAt;
}