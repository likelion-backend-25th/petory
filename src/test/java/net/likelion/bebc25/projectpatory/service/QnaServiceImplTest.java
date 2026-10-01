package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PostCreateRequest;
import net.likelion.bebc25.projectpatory.dto.PostCreateResponse;
import net.likelion.bebc25.projectpatory.dto.PostDetailResponse;
import net.likelion.bebc25.projectpatory.dto.PostListResponse;
import net.likelion.bebc25.projectpatory.dto.PostUpdateRequest;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import net.likelion.bebc25.projectpatory.mapper.QnaMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class QnaServiceImplTest {

    @Mock
    private QnaMapper qnaMapper;

    @Mock
    private PostService postService;

    @InjectMocks
    private QnaServiceImpl qnaService;

    @Nested
    @DisplayName("QnA 목록 조회")
    class GetQnaListCursorTest {

        @Test
        @DisplayName("size보다 많이 조회되면 hasNext=true이고 size개만 반환한다")
        void getQnaListCursor_whenHasNext_returnsSlicedResult() {
            List<PostListResponse> rows = new ArrayList<>();
            for (long i = 3; i >= 1; i--) {
                rows.add(PostListResponse.builder().id(i).type(2).content("qna-" + i).build());
            }
            given(qnaMapper.selectQnaListCursor(null, 3)).willReturn(rows);

            SliceResponse<PostListResponse> response = qnaService.getQnaListCursor(null, 2);

            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getLastPostId()).isEqualTo(2L);
            verify(qnaMapper).selectQnaListCursor(null, 3);
        }

        @Test
        @DisplayName("size 이하로 조회되면 hasNext=false이다")
        void getQnaListCursor_whenNoNext_returnsHasNextFalse() {
            List<PostListResponse> rows = new ArrayList<>();
            rows.add(PostListResponse.builder().id(1L).type(2).content("only").build());
            given(qnaMapper.selectQnaListCursor(null, 3)).willReturn(rows);

            SliceResponse<PostListResponse> response = qnaService.getQnaListCursor(null, 2);

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getLastPostId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("QnA 해시태그 검색")
    class SearchQnaByHashtagTest {

        @Test
        @DisplayName("앞에 #이 있어도 제거하고 검색한다")
        void searchQnaByHashtag_whenHashPrefix_stripsAndSearches() {
            given(qnaMapper.selectQnaListByHashtag("건강", null, 11))
                    .willReturn(new ArrayList<>());

            qnaService.searchQnaByHashtag("#건강", null, 10);

            verify(qnaMapper).selectQnaListByHashtag("건강", null, 11);
        }

        @Test
        @DisplayName("빈 검색어면 IllegalArgumentException을 던진다")
        void searchQnaByHashtag_whenBlank_throwsException() {
            assertThatThrownBy(() -> qnaService.searchQnaByHashtag("  ", null, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해시태그");

            verifyNoInteractions(qnaMapper);
        }

        @Test
        @DisplayName("해시태그가 여러 개면 IllegalArgumentException을 던진다")
        void searchQnaByHashtag_whenMultiple_throwsException() {
            assertThatThrownBy(() -> qnaService.searchQnaByHashtag("건강 훈련", null, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("한 개");

            verifyNoInteractions(qnaMapper);
        }
    }

    @Nested
    @DisplayName("QnA 작성")
    class CreateQnaTest {

        @Test
        @DisplayName("작성 성공 시 생성된 ID를 반환하고 이미지도 저장한다")
        void createQna_withImages_insertsQnaAndImages() {
            PostCreateRequest request = PostCreateRequest.builder()
                    .memberId(1L)
                    .content("연고 추천 부탁드려요")
                    .hashtags("#건강 #강아지")
                    .imageUrls(List.of("https://example.com/a.jpg"))
                    .build();

            willAnswer(invocation -> {
                PostCreateRequest req = invocation.getArgument(0);
                ReflectionTestUtils.setField(req, "id", 40L);
                return null;
            }).given(qnaMapper).insertQna(any(PostCreateRequest.class));

            PostCreateResponse response = qnaService.createQna(request);

            assertThat(response.getId()).isEqualTo(40L);
            verify(qnaMapper).insertQna(any(PostCreateRequest.class));
            verify(qnaMapper).insertQnaImages(40L, List.of("https://example.com/a.jpg"));
        }

        @Test
        @DisplayName("이미지가 없으면 insertQnaImages를 호출하지 않는다")
        void createQna_withoutImages_skipsImageInsert() {
            PostCreateRequest request = PostCreateRequest.builder()
                    .memberId(1L)
                    .content("질문만")
                    .build();

            willAnswer(invocation -> {
                PostCreateRequest req = invocation.getArgument(0);
                ReflectionTestUtils.setField(req, "id", 41L);
                return null;
            }).given(qnaMapper).insertQna(any(PostCreateRequest.class));

            qnaService.createQna(request);

            verify(qnaMapper).insertQna(any(PostCreateRequest.class));
            verify(qnaMapper, never()).insertQnaImages(any(), any());
        }
    }

    @Nested
    @DisplayName("QnA 상세 조회")
    class GetQnaDetailTest {

        @Test
        @DisplayName("type=2이면 PostService 상세 조회를 위임한다")
        void getQnaDetail_whenTypeIsQna_delegatesToPostService() {
            Long postId = 4L;
            Long memberId = 1L;
            PostDetailResponse detail = new PostDetailResponse();
            ReflectionTestUtils.setField(detail, "id", postId);

            given(qnaMapper.selectPostType(postId)).willReturn(2);
            given(postService.getPostDetail(postId, memberId)).willReturn(detail);

            PostDetailResponse response = qnaService.getQnaDetail(postId, memberId);

            assertThat(response.getId()).isEqualTo(postId);
            verify(postService).getPostDetail(postId, memberId);
        }

        @Test
        @DisplayName("type이 2가 아니면 NoSuchElementException을 던진다")
        void getQnaDetail_whenTypeIsFeed_throwsException() {
            given(qnaMapper.selectPostType(1L)).willReturn(1);

            assertThatThrownBy(() -> qnaService.getQnaDetail(1L, 1L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("QnA");

            verify(postService, never()).getPostDetail(any(), any());
        }

        @Test
        @DisplayName("게시글이 없으면 NoSuchElementException을 던진다")
        void getQnaDetail_whenMissing_throwsException() {
            given(qnaMapper.selectPostType(99L)).willReturn(null);

            assertThatThrownBy(() -> qnaService.getQnaDetail(99L, 1L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("찾을 수 없습니다");

            verifyNoInteractions(postService);
        }
    }

    @Nested
    @DisplayName("QnA 수정")
    class UpdateQnaTest {

        @Test
        @DisplayName("수정 시 isSubscriberOnly를 0으로 강제하고 PostService에 위임한다")
        void updateQna_forcesSubscriberOnlyZero() {
            Long postId = 4L;
            Long memberId = 1L;
            given(qnaMapper.selectPostType(postId)).willReturn(2);

            PostUpdateRequest request = PostUpdateRequest.builder()
                    .content("수정된 질문")
                    .bgmUrl(null)
                    .hashtags("#건강")
                    .isSubscriberOnly(1)
                    .build();

            qnaService.updateQna(postId, memberId, request);

            ArgumentCaptor<PostUpdateRequest> captor = ArgumentCaptor.forClass(PostUpdateRequest.class);
            verify(postService).updatePost(eq(postId), eq(memberId), captor.capture());
            assertThat(captor.getValue().getIsSubscriberOnly()).isEqualTo(0);
            assertThat(captor.getValue().getContent()).isEqualTo("수정된 질문");
            assertThat(captor.getValue().getHashtags()).isEqualTo("#건강");
        }

        @Test
        @DisplayName("type이 2가 아니면 수정하지 않는다")
        void updateQna_whenNotQna_throwsException() {
            given(qnaMapper.selectPostType(1L)).willReturn(1);

            assertThatThrownBy(() -> qnaService.updateQna(1L, 1L,
                    PostUpdateRequest.builder().content("x").build()))
                    .isInstanceOf(NoSuchElementException.class);

            verify(postService, never()).updatePost(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("QnA 삭제")
    class DeleteQnaTest {

        @Test
        @DisplayName("type=2이면 PostService 삭제를 위임한다")
        void deleteQna_whenTypeIsQna_delegatesToPostService() {
            given(qnaMapper.selectPostType(4L)).willReturn(2);

            qnaService.deleteQna(4L, 1L);

            verify(postService).deletePost(4L, 1L);
        }

        @Test
        @DisplayName("type이 2가 아니면 삭제하지 않는다")
        void deleteQna_whenNotQna_throwsException() {
            given(qnaMapper.selectPostType(1L)).willReturn(1);

            assertThatThrownBy(() -> qnaService.deleteQna(1L, 1L))
                    .isInstanceOf(NoSuchElementException.class);

            verify(postService, never()).deletePost(any(), any());
        }
    }
}
