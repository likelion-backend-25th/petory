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
@Schema(description = "S3 Presigned URL 발급 응답")
public class PresignUploadResponse {

    @Schema(
            description = "클라이언트가 파일을 PUT 업로드할 Presigned URL (만료 시간이 있음)",
            example = "https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/uploads/posts/1/uuid.jpg?X-Amz-Algorithm=..."
    )
    private String uploadUrl;

    @Schema(
            description = "업로드 완료 후 DB/게시글에 저장할 공개 파일 URL",
            example = "https://projectpatory-s3-bucket-2026.s3.ap-northeast-2.amazonaws.com/uploads/posts/1/uuid.jpg"
    )
    private String fileUrl;

    @Schema(
            description = "S3 객체 키 (삭제 시 사용)",
            example = "uploads/posts/1/550e8400-e29b-41d4-a716-446655440000.jpg"
    )
    private String key;
}
