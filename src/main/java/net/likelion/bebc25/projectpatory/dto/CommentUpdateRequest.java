package net.likelion.bebc25.projectpatory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "댓글 수정 요청")
public class CommentUpdateRequest {

    @NotBlank(message = "댓글 본문은 비어 있을 수 없습니다.")
    @Schema(description = "수정할 댓글 본문", example = "내용을 조금 고쳤습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
