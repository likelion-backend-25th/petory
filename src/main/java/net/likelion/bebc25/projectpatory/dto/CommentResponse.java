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
@Schema(description = "댓글 조회 응답 DTO")
public class CommentResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long id;

    @Schema(description = "댓글 작성자 회원 ID", example = "3")
    private Long commenterId;

    @Schema(description = "댓글 작성자 닉네임", example = "나비")
    private String commenterNickname;

    @Schema(description = "댓글 본문", example = "한강 너무 좋겠다! 다음에 같이 가요~")
    private String content;

    @Schema(description = "댓글 작성 일시", example = "2025-07-01T10:30:00")
    private LocalDateTime createdAt;
}
