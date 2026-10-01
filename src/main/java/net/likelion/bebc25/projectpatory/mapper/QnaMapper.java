package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QnaMapper {

    /**
     * 메인 피드 목록 조회 (커서 기반 무한 스크롤 - Qna 피드 p.type = 2 전용)
     *
     * @param lastPostId 직전 목록의 마지막 게시글 ID (처음 요청 시 null)
     * @param limit      가져올 게시글 개수 (size + 1)
     */
    List<PostListResponse> selectQnaListCursor(
            @Param("lastPostId") Long lastPostId,
            @Param("limit") int limit
    );

    /**
     * 해시태그 검색 (커서 기반 무한 스크롤 - Qna 피드 p.type = 2 전용)
     *
     * @param hashtag    '#' 을 뺀 검색어 (예: 강아지)
     * @param lastPostId 직전 목록의 마지막 게시글 ID (처음 요청 시 null)
     * @param limit      가져올 게시글 개수 (size + 1)
     */
    List<PostListResponse> selectQnaListByHashtag(
            @Param("hashtag") String hashtag,
            @Param("lastPostId") Long lastPostId,
            @Param("limit") int limit
    );

    void insertQna(PostCreateRequest request);

    // 게시글 이미지 첨부
    void insertQnaImages(@Param("postId") Long postId, @Param("imageUrls") List<String> imageUrls);

    // 메인 피드/qna 게시물 검증용
    Integer selectPostType(@Param("postId") Long postId);
}