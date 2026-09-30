package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PresignUploadRequest;
import net.likelion.bebc25.projectpatory.dto.PresignUploadResponse;

import java.util.List;

public interface S3Service {

    /**
     * 게시글/미디어 업로드용 S3 Presigned PUT URL을 발급한다.
     *
     * @param memberId 업로드를 요청한 회원 ID (객체 key 경로에 사용)
     * @param request  원본 파일명, Content-Type
     */
    PresignUploadResponse createPresignedUpload(Long memberId, PresignUploadRequest request);

    // file url로 이미지 삭제
    void deleteObjectsByFileUrls(List<String> fileUrls);
}
