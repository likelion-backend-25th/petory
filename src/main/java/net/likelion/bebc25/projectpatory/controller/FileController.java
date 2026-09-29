package net.likelion.bebc25.projectpatory.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.PresignUploadRequest;
import net.likelion.bebc25.projectpatory.dto.PresignUploadResponse;
import net.likelion.bebc25.projectpatory.security.principal.CustomUserDetails;
import net.likelion.bebc25.projectpatory.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    @Operation(
            summary = "S3 Presigned URL 발급",
            description = "클라이언트가 S3에 직접 PUT 업로드할 수 있도록 임시 URL을 발급합니다."
    )
    @PostMapping("/presign")
    public ResponseEntity<PresignUploadResponse> createPresignedUpload(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PresignUploadRequest request
    ) {
        PresignUploadResponse response = s3Service.createPresignedUpload(userDetails.getId(), request);
        return ResponseEntity.ok(response);
    }
}
