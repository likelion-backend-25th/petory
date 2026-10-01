package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.*;

import java.util.List;

public interface QnaService {
    /**
     * Qna 피드 커서 기반 무한 스크롤 조회
     */
    SliceResponse<PostListResponse> getQnaListCursor(Long lastPostId, int size);

    /**
     * 해시태그 검색 (커서 기반 무한 스크롤)
     */
    SliceResponse<PostListResponse> searchQnaByHashtag(String hashtag, Long lastPostId, int size);

    /**
     * 게시글 상세 조회 (단건 조회)
     * + 고유 조회수 처리를 위해 멤버 정보를 받아와야해서 memberId를 인자로 추가
     */
    PostDetailResponse getQnaDetail(Long postId, Long memberId);

    PostCreateResponse createQna(PostCreateRequest request);

    /**
     * 게시글 삭제
     */
    void updateQna(Long postId, Long memberId, PostUpdateRequest request);
    void deleteQna(Long postId, Long memberId);
}