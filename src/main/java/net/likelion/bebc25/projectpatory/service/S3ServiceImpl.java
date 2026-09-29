package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.PresignUploadRequest;
import net.likelion.bebc25.projectpatory.dto.PresignUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private static final Duration PRESIGN_EXPIRATION = Duration.ofMinutes(10);
    private static final String KEY_PREFIX = "uploads/posts";
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "audio/mpeg",
            "audio/mp4",
            "audio/wav"
    );

    // Presigned 업로드 URL 발급
    private final S3Presigner s3Presigner;
    // deleteObjects 실제 삭제
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Override
    public PresignUploadResponse createPresignedUpload(Long memberId, PresignUploadRequest request) {
        String contentType = request.getContentType().trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않은 Content-Type 입니다: " + contentType);
        }

        String extension = extractExtension(request.getFilename());
        String key = KEY_PREFIX + "/" + memberId + "/" + UUID.randomUUID() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
        String uploadUrl = presigned.url().toString();
        String fileUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;

        return PresignUploadResponse.builder()
                .uploadUrl(uploadUrl)
                .fileUrl(fileUrl)
                .key(key)
                .build();
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        String ext = filename.substring(dotIndex).toLowerCase(Locale.ROOT);
        // path traversal / 이상 문자 방지: 확장자는 영숫자만 허용
        if (!ext.matches("^\\.[a-z0-9]{1,10}$")) {
            throw new IllegalArgumentException("올바르지 않은 파일 확장자입니다: " + filename);
        }
        return ext;
    }

    @Override
    public void deleteObjectsByFileUrls(List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        String baseUrl = "https://" + bucket + ".s3." + region + ".amazonaws.com/";

        for (String fileUrl : fileUrls) {
            if (fileUrl == null || fileUrl.isBlank() || !fileUrl.startsWith(baseUrl)) {
                continue;
            }

            String key = fileUrl.substring(baseUrl.length());
            if (key.isBlank()) {
                continue;
            }

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        }
    }
}
