package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.MyPagePostResponse;
import net.likelion.bebc25.projectpatory.dto.PostCreateRequest;
import net.likelion.bebc25.projectpatory.dto.PostDetailResponse;
import net.likelion.bebc25.projectpatory.dto.PostListResponse;
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

    PostDetailResponse selectPostDetail(Long postId);

    long countMyPosts(Long memberId);

    List<MyPagePostResponse> getMyMainPosts(Long memberId);

    List<MyPagePostResponse> getMyQnAPosts(Long memberId);

    List<MyPagePostResponse> getMyBookmarks(Long memberId);

    void insertPost(PostCreateRequest request);
}