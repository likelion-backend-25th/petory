package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 작성 요청")
public class CommentCreateRequest {

    @Schema(description = "댓글 ID (INSERT 후 자동 생성됨)", hidden = true)
    private Long id;

    @Schema(description = "게시글 ID", hidden = true)
    private Long postId;

    @Schema(description = "작성자 회원 ID", hidden = true)
    private Long memberId;

    @NotBlank(message = "댓글 본문은 비어 있을 수 없습니다.")
    @Schema(description = "댓글 본문", example = "좋은 글 잘 읽었습니다!", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
