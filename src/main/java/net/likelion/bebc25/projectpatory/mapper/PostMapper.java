package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.MyPagePostResponse;
import net.likelion.bebc25.projectpatory.dto.PostCreateRequest;
import net.likelion.bebc25.projectpatory.dto.PostDetailResponse;
import net.likelion.bebc25.projectpatory.dto.PostListResponse;
import net.likelion.bebc25.projectpatory.dto.PostUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    /**
     * 메인 피드 목록 조회 (커서 기반 무한 스크롤 - 일반 피드 p.type = 1 전용)
     *
     * @param lastPostId 직전 목록의 마지막 게시글 ID (처음 요청 시 null)
     * @param limit      가져올 게시글 개수 (size + 1)
     */
    List<PostListResponse> selectPostListCursor(
            @Param("lastPostId") Long lastPostId,
            @Param("limit") int limit
    );

    /**
     * 해시태그 검색 (커서 기반 무한 스크롤 - 일반 피드 p.type = 1 전용)
     *
     * @param hashtag    '#' 을 뺀 검색어 (예: 강아지)
     * @param lastPostId 직전 목록의 마지막 게시글 ID (처음 요청 시 null)
     * @param limit      가져올 게시글 개수 (size + 1)
     */
    List<PostListResponse> selectPostListByHashtag(
            @Param("hashtag") String hashtag,
            @Param("lastPostId") Long lastPostId,
            @Param("limit") int limit
    );

    PostDetailResponse selectPostDetail(Long postId);

    long countMyPosts(Long memberId);

    List<MyPagePostResponse> getMyMainPosts(Long memberId);

    List<MyPagePostResponse> getMyQnAPosts(Long memberId);

    List<MyPagePostResponse> getMyBookmarks(Long memberId);

    void insertPost(PostCreateRequest request);

    // 게시글 수정 (성공 시 수정된 행 수 반환)
    int updatePost(@Param("postId") Long postId, @Param("memberId") Long memberId, @Param("request") PostUpdateRequest request);

    // 게시글 삭제 (성공 시 삭제된 행 수 반환)
    int deletePost(@Param("postId") Long postId, @Param("memberId") Long memberId);

    // 게시글 이미지 첨부
    void insertPostImages(@Param("postId") Long postId, @Param("imageUrls") List<String> imageUrls);

    // 게시글 삭제시 같이 삭제되어야할 이미지 url select
    List<String> selectImageUrlsByPostId(@Param("postId") Long postId);
}