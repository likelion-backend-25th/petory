package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PostDetailResponse;
import net.likelion.bebc25.projectpatory.dto.PostListResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;

public interface PostService {
    /**
     * 메인 피드 커서 기반 무한 스크롤 조회
     */
    SliceResponse<PostListResponse> getPostListCursor(Long lastPostId, int size);

    /**
     * 게시글 상세 조회 (단건 조회)
     */
    PostDetailResponse getPostDetail(Long postId);
}