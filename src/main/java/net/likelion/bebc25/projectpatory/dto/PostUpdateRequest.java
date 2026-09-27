package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {

    @Schema(description = "수정할 게시글 내용", example = "수정된 게시글 내용입니다.")
    private String content;

    @Schema(description = "BGM URL", example = "https://example.com/new_bgm.mp3")
    private String bgmUrl;

    @Schema(description = "구독자 전용 여부 (0: 전체공개, 1: 구독자전용)", example = "0")
    private Integer isSubscriberOnly;

    @Schema(description = "해시태그", example = "#산책 #일상 #수정")
    private String hashtags;
}