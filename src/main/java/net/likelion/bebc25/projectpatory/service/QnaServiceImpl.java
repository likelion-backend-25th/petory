package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.mapper.CommentMapper;
import net.likelion.bebc25.projectpatory.mapper.PostInteractionMapper;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import net.likelion.bebc25.projectpatory.mapper.QnaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaServiceImpl implements QnaService {

    private final QnaMapper qnaMapper;
    private final PostService postService;

    // post_type = 2(qna)인지 검증
    private void assertQnaType(Long postId) {
        Integer type = qnaMapper.selectPostType(postId);
        if (type == null) {
            throw new NoSuchElementException("해당 ID의 게시글을 찾을 수 없습니다. id=" + postId);
        }
        if (type != 2) {
            throw new NoSuchElementException("QnA 게시글이 아닙니다. id=" + postId);
        }
    }

    @Override
    public SliceResponse<PostListResponse> getQnaListCursor(Long lastPostId, int size) {
        int limit = size + 1;
        List<PostListResponse> posts = qnaMapper.selectQnaListCursor(lastPostId, limit);

        boolean hasNext = false;
        if (posts.size() > size) {
            hasNext = true;
            posts.remove(size);
        }

        Long nextCursorId = posts.isEmpty() ? null : posts.get(posts.size() - 1).getId();

        return new SliceResponse<>(posts, hasNext, nextCursorId);
    }

    @Override
    public SliceResponse<PostListResponse> searchQnaByHashtag(String hashtag, Long lastPostId, int size) {
        // 1. 검색어가 비어 있으면 거절한다
        if (hashtag == null || hashtag.isBlank()) {
            throw new IllegalArgumentException("검색할 해시태그를 입력해주세요.");
        }

        // 2. 앞뒤 공백을 지우고, 맨 앞의 '#' 은 떼어낸다 (#강아지, 강아지 둘 다 같은 검색이 되도록)
        String keyword = hashtag.trim();
        if (keyword.startsWith("#")) {
            keyword = keyword.substring(1);
        }

        // 3. '#' 만 입력했거나, 태그를 여러 개 넣은 경우는 거절한다 (태그 1개 검색만 지원)
        if (keyword.isEmpty()) {
            throw new IllegalArgumentException("검색할 해시태그를 입력해주세요.");
        }
        if (keyword.contains(" ") || keyword.contains("#")) {
            throw new IllegalArgumentException("해시태그는 한 개만 검색할 수 있습니다.");
        }

        // 4. 메인 피드와 같은 방식으로 1개 더 조회해서 다음 페이지가 있는지 확인한다
        int limit = size + 1;
        List<PostListResponse> posts = qnaMapper.selectQnaListByHashtag(keyword, lastPostId, limit);

        boolean hasNext = false;
        if (posts.size() > size) {
            hasNext = true;
            posts.remove(size);
        }

        Long nextCursorId = posts.isEmpty() ? null : posts.get(posts.size() - 1).getId();

        return new SliceResponse<>(posts, hasNext, nextCursorId);
    }

    @Transactional
    @Override
    // 고유 조회수 처리를 위해 멤버 정보를 받아와야해서 memberId를 인자로 추가
    public PostDetailResponse getQnaDetail(Long postId, Long memberId) {
        assertQnaType(postId);
        return postService.getPostDetail(postId, memberId); // 댓글, 조회수 포함
    }

    @Transactional
    @Override
    public PostCreateResponse createQna(PostCreateRequest request) {
        qnaMapper.insertQna(request);

        List<String> imageUrls = request.getImageUrls();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            qnaMapper.insertQnaImages(request.getId(), imageUrls);
        }
        return PostCreateResponse.builder().id(request.getId()).build();
    }

    @Override
    @Transactional
    public void updateQna(Long postId, Long memberId, PostUpdateRequest request) {
        assertQnaType(postId);
        PostUpdateRequest forced = PostUpdateRequest.builder()
                .content(request.getContent())
                .bgmUrl(request.getBgmUrl())
                .hashtags(request.getHashtags())
                .isSubscriberOnly(0) // 구독자 전용 옵션 비활성화
                .build();
        postService.updatePost(postId, memberId, forced);
    }

    @Override
    @Transactional
    public void deleteQna(Long postId, Long memberId) {
        assertQnaType(postId);
        postService.deletePost(postId, memberId); // 이미지 URL·S3 삭제 포함
    }
}