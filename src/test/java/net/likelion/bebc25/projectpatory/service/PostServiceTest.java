package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PostCreateRequest;
import net.likelion.bebc25.projectpatory.dto.PostCreateResponse;
import net.likelion.bebc25.projectpatory.dto.PostListResponse;
import net.likelion.bebc25.projectpatory.dto.PostUpdateRequest;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private PostServiceImpl postService;

    // 1. 게시글 생성 테스트
    @Test
    @DisplayName("게시글 작성 성공 - 생성된 게시글 ID를 정상 반환한다")
    void createPost_Success() {
        // given (준비)
        PostCreateRequest request = PostCreateRequest.builder()
                .memberId(1L)
                .content("단위 테스트용 게시글 내용입니다.")
                .bgmUrl("https://example.com/bgm.mp3")
                .isSubscriberOnly(0)
                .hashtags("#테스트 #단위테스트")
                .build();

        // MyBatis의 useGeneratedKeys 동작 흉내내기 (insertPost 호출 시 request의 id에 100L 세팅)
        willAnswer(invocation -> {
            PostCreateRequest req = invocation.getArgument(0);
            ReflectionTestUtils.setField(req, "id", 100L);
            return null;
        }).given(postMapper).insertPost(any(PostCreateRequest.class));

        // when (실행)
        PostCreateResponse response = postService.createPost(request);

        // then (검증)
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);

        // postMapper.insertPost가 1회 호출되었는지 검증
        verify(postMapper).insertPost(any(PostCreateRequest.class));
    }

    // 2. 게시글 수정 테스트
    @Nested
    @DisplayName("게시글 수정 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("성공 - 본인의 게시글 수정 시 정상 처리된다")
        void updatePost_Success() {
            // given
            Long postId = 7L;
            Long memberId = 1L;
            PostUpdateRequest request = PostUpdateRequest.builder()
                    .content("수정된 내용입니다.")
                    .bgmUrl("https://example.com/new_bgm.mp3")
                    .isSubscriberOnly(0)
                    .hashtags("#수정 #테스트")
                    .build();

            given(postMapper.updatePost(eq(postId), eq(memberId), any(PostUpdateRequest.class)))
                    .willReturn(1);

            // when
            postService.updatePost(postId, memberId, request);

            // then
            verify(postMapper).updatePost(eq(postId), eq(memberId), any(PostUpdateRequest.class));
        }

        @Test
        @DisplayName("실패 - 게시글이 없거나 작성자가 일치하지 않으면 예외가 발생한다")
        void updatePost_NotFoundOrNoPermission_ThrowsException() {
            // given
            Long postId = 7L;
            Long memberId = 999L; // 작성자가 아님
            PostUpdateRequest request = PostUpdateRequest.builder()
                    .content("수정 시도")
                    .build();

            given(postMapper.updatePost(eq(postId), eq(memberId), any(PostUpdateRequest.class)))
                    .willReturn(0); // 영향받은 행 0개 (실패)

            // when & then
            assertThatThrownBy(() -> postService.updatePost(postId, memberId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("게시글을 찾을 수 없거나 수정 권한이 없습니다.");
        }
    }

    // 3. 게시글 삭제 테스트
    @Nested
    @DisplayName("게시글 삭제 테스트")
    class DeletePostTest {

        @Test
        @DisplayName("성공 - 본인의 게시글 삭제 시 정상 처리된다")
        void deletePost_Success() {
            // given
            Long postId = 7L;
            Long memberId = 1L;
            List<String> imageUrls = List.of("https://s3.example.com/1.jpg", "https://s3.example.com/2.jpg");

            given(postMapper.selectImageUrlsByPostId(postId))
                    .willReturn(imageUrls);
            given(postMapper.deletePost(eq(postId), eq(memberId)))
                    .willReturn(1);

            // when
            postService.deletePost(postId, memberId);

            // then
            verify(postMapper).deletePost(eq(postId), eq(memberId));
            verify(s3Service).deleteObjectsByFileUrls(imageUrls);
        }

        @Test
        @DisplayName("실패 - 게시글이 없거나 작성자가 일치하지 않으면 예외가 발생한다")
        void deletePost_NotFoundOrNoPermission_ThrowsException() {
            // given
            Long postId = 7L;
            Long memberId = 999L;

            given(postMapper.deletePost(eq(postId), eq(memberId)))
                    .willReturn(0); // 영향받은 행 0개 (실패)

            // when & then
            assertThatThrownBy(() -> postService.deletePost(postId, memberId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("게시글을 찾을 수 없거나 삭제 권한이 없습니다.");

            // 권한이 없으면 S3 이미지는 지우면 안 된다
            verify(s3Service, never()).deleteObjectsByFileUrls(any());
        }
    }

    // 4. 해시태그 검색 테스트
    @Nested
    @DisplayName("해시태그 검색 테스트")
    class SearchPostsByHashtagTest {

        private List<PostListResponse> createPosts(long... ids) {
            List<PostListResponse> posts = new ArrayList<>();
            for (long id : ids) {
                posts.add(PostListResponse.builder().id(id).build());
            }
            return posts;
        }

        @Test
        @DisplayName("성공 - 앞의 '#' 과 공백을 지운 검색어로 조회한다")
        void search_RemovesHashAndSpaces() {
            // given
            given(postMapper.selectPostListByHashtag("강아지", null, 11))
                    .willReturn(createPosts(3L, 2L));

            // when
            SliceResponse<PostListResponse> response = postService.searchPostsByHashtag("  #강아지 ", null, 10);

            // then
            verify(postMapper).selectPostListByHashtag("강아지", null, 11);
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getLastPostId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("성공 - size 보다 1개 더 조회되면 hasNext 가 true 이고 마지막 1개는 버린다")
        void search_HasNext() {
            // given (size = 2 인데 3개가 조회됨)
            given(postMapper.selectPostListByHashtag("강아지", null, 3))
                    .willReturn(createPosts(5L, 4L, 3L));

            // when
            SliceResponse<PostListResponse> response = postService.searchPostsByHashtag("강아지", null, 2);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getLastPostId()).isEqualTo(4L);
        }

        @Test
        @DisplayName("성공 - 결과가 없으면 빈 리스트와 lastPostId null 을 반환한다")
        void search_NoResult() {
            // given
            given(postMapper.selectPostListByHashtag("없는태그", null, 11))
                    .willReturn(new ArrayList<>());

            // when
            SliceResponse<PostListResponse> response = postService.searchPostsByHashtag("없는태그", null, 10);

            // then
            assertThat(response.getContent()).isEmpty();
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getLastPostId()).isNull();
        }

        @Test
        @DisplayName("실패 - 검색어가 비어 있거나 '#' 만 있으면 예외가 발생한다")
        void search_EmptyKeyword_ThrowsException() {
            assertThatThrownBy(() -> postService.searchPostsByHashtag(null, null, 10))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> postService.searchPostsByHashtag("   ", null, 10))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> postService.searchPostsByHashtag("#", null, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("실패 - 태그를 여러 개 넣으면 예외가 발생한다")
        void search_MultipleTags_ThrowsException() {
            assertThatThrownBy(() -> postService.searchPostsByHashtag("#강아지 #산책", null, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해시태그는 한 개만 검색할 수 있습니다.");
            assertThatThrownBy(() -> postService.searchPostsByHashtag("강아지#산책", null, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}