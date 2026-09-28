package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    @Override
    public SliceResponse<PostListResponse> getPostListCursor(Long lastPostId, int size) {
        int limit = size + 1;
        List<PostListResponse> posts = postMapper.selectPostListCursor(lastPostId, limit);

        boolean hasNext = false;
        if (posts.size() > size) {
            hasNext = true;
            posts.remove(size);
        }

        Long nextCursorId = posts.isEmpty() ? null : posts.get(posts.size() - 1).getId();

        return new SliceResponse<>(posts, hasNext, nextCursorId);
    }

    @Override
    public PostDetailResponse getPostDetail(Long postId) {
        // MyBatis Mapper를 통해 게시글 단건 및 member 조인 데이터 조회
        PostDetailResponse postDetail = postMapper.selectPostDetail(postId);

        // 게시글이 존재하지 않으면 NoSuchElementException 던짐 -> GlobalRestExceptionHandler가 404로 처리
        if (postDetail == null) {
            throw new NoSuchElementException("해당 ID의 게시글을 찾을 수 없습니다. id=" + postId);
        }

        return postDetail;
    }

    @Transactional
    @Override
    public PostCreateResponse createPost(PostCreateRequest request) {
        // 1. 게시글 데이터 INSERT (useGeneratedKeys 설정으로 request.getId()에 자동 세팅됨)
        postMapper.insertPost(request);

        // 2. 생성된 ID를 담아 응답 DTO 반환
        return PostCreateResponse.builder()
                .id(request.getId())
                .build();
    }

    // 이 아래로 3개는 마이페이지에서 내가 작성한 메인피드, QnA, 내가 북마크 게시글을 불러오는 서비스임
    // 상대 프로필 정보를 볼 때도 활용 가능
    // 다만 상대프로필에서 메인포스트 목록 조회시 구독자 전용 게시글의 경우 대표이미지를 블러처리하기 위한 로직을 구현해 주어야함
    // 무한 스크롤 기능은 적용하지 않은 상태임 (추후 적용 검토)
    @Override
    public List<MyPagePostResponse> getMyMainPosts(Long memberId) {
        return postMapper.getMyMainPosts(memberId);
    }

    @Override
    public List<MyPagePostResponse> getMyQnAPosts(Long memberId) {
        return postMapper.getMyQnAPosts(memberId);
    }

    @Override
    public List<MyPagePostResponse> getMyBookmarks(Long memberId, Long loginMemberId) {
        if (!loginMemberId.equals(memberId)) {
            throw new IllegalStateException("본인의 북마크만 조회할 수 있습니다.");
        }
        return postMapper.getMyBookmarks(memberId);
    }
}