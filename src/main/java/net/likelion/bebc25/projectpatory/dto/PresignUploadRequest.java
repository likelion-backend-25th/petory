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
@Schema(description = "S3 Presigned URL 발급 요청")
public class PresignUploadRequest {

    @NotBlank(message = "파일명은 필수입니다.")
    @Schema(description = "원본 파일명 (확장자 추출용)", example = "dog.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String filename;

    @NotBlank(message = "Content-Type은 필수입니다.")
    @Schema(description = "파일 Content-Type", example = "image/jpeg", requiredMode = Schema.RequiredMode.REQUIRED)
    private String contentType;
}
