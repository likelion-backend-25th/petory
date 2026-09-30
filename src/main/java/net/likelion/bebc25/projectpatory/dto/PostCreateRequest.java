package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 생성 요청")
public class PostCreateRequest {
    @Schema(description = "게시글 ID (INSERT 후 자동 생성됨)", hidden = true)
    private Long id;

    // 변경: 클라이언트 입력 대신 컨트롤러에서 JWT 인증 사용자 ID로 세팅 (Swagger 숨김 + setter 추가)
    @Schema(description = "작성자 회원 ID (JWT 인증 사용자로 자동 세팅)", hidden = true)
    @Setter
    private Long memberId;

    @Schema(description = "게시글 본문 내용", example = "오늘 새로운 산책 코스를 발견했어요!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "배경음악 URL", example = "https://example.com/bgm.mp3")
    private String bgmUrl;

    @Schema(description = "구독자 전용 여부 (0: 전체공개, 1: 구독자전용)", example = "0")
    private Integer isSubscriberOnly;

    @Schema(description = "해시태그 목록", example = "#산책 #일상 #힐링")
    private String hashtags;

    @Schema(description = "이미지 URL",
            example = "[\"https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/uploads/posts/1/uuid1.jpg\"]")
    private List<String> imageUrls;
}