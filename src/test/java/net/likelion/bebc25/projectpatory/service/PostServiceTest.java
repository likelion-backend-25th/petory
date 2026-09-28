package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PostCreateRequest;
import net.likelion.bebc25.projectpatory.dto.PostCreateResponse;
import net.likelion.bebc25.projectpatory.dto.PostUpdateRequest;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

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

            given(postMapper.deletePost(eq(postId), eq(memberId)))
                    .willReturn(1);

            // when
            postService.deletePost(postId, memberId);

            // then
            verify(postMapper).deletePost(eq(postId), eq(memberId));
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
        }
    }
}