package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.CommentResponse;
import net.likelion.bebc25.projectpatory.dto.CommentUpdateRequest;
import net.likelion.bebc25.projectpatory.dto.PostDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Nested
    @DisplayName("댓글 수정")
    class UpdateCommentTest {

        @Test
        @DisplayName("성공 - 본인 댓글 본문이 변경되고 게시글 상세에 반영된다")
        void updateComment_Success() {
            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content("한강 너무 좋겠다! 일정도 다시 조율해봐요.")
                    .build();

            CommentResponse updated = commentService.updateComment(1L, 1L, 3L, request);

            assertThat(updated.getId()).isEqualTo(1L);
            assertThat(updated.getCommenterId()).isEqualTo(3L);
            assertThat(updated.getContent()).isEqualTo("한강 너무 좋겠다! 일정도 다시 조율해봐요.");

            PostDetailResponse detail = postService.getPostDetail(1L, null);
            assertThat(detail.getComments())
                    .filteredOn(comment -> comment.getId().equals(1L))
                    .extracting(CommentResponse::getContent)
                    .containsExactly("한강 너무 좋겠다! 일정도 다시 조율해봐요.");
        }

        @Test
        @DisplayName("실패 - 타인 댓글이면 IllegalArgumentException")
        void updateComment_NoPermission() {
            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content("남의 댓글을 수정할 수 없다")
                    .build();

            assertThatThrownBy(() -> commentService.updateComment(1L, 1L, 2L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글을 찾을 수 없거나 수정 권한이 없습니다. commentId: 1");
        }

        @Test
        @DisplayName("실패 - 다른 게시글 경로면 IllegalArgumentException")
        void updateComment_WrongPostId() {
            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content("게시글이 다른 댓글")
                    .build();

            assertThatThrownBy(() -> commentService.updateComment(2L, 1L, 3L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글을 찾을 수 없거나 수정 권한이 없습니다. commentId: 1");
        }

        @Test
        @DisplayName("실패 - 존재하지 않으면 IllegalArgumentException")
        void updateComment_NotFound() {
            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content("없는 댓글")
                    .build();

            assertThatThrownBy(() -> commentService.updateComment(1L, 999999L, 2L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글을 찾을 수 없거나 수정 권한이 없습니다. commentId: 999999");
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteCommentTest {

        @Test
        @DisplayName("성공 - 게시글 상세 댓글 목록에서 제거된다")
        void deleteComment_Success() {
            commentService.deleteComment(1L, 1L, 3L);

            PostDetailResponse detail = postService.getPostDetail(1L, null);
            assertThat(detail.getComments()).hasSize(1);
            assertThat(detail.getComments())
                    .extracting(CommentResponse::getId)
                    .doesNotContain(1L);
        }

        @Test
        @DisplayName("실패 - 타인 댓글이면 IllegalArgumentException")
        void deleteComment_NoPermission() {
            assertThatThrownBy(() -> commentService.deleteComment(1L, 1L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글을 찾을 수 없거나 삭제 권한이 없습니다. commentId: 1");
        }

        @Test
        @DisplayName("실패 - 다른 게시글 경로면 IllegalArgumentException")
        void deleteComment_WrongPostId() {
            assertThatThrownBy(() -> commentService.deleteComment(2L, 1L, 3L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글을 찾을 수 없거나 삭제 권한이 없습니다. commentId: 1");
        }

        @Test
        @DisplayName("실패 - 존재하지 않으면 IllegalArgumentException")
        void deleteComment_NotFound() {
            assertThatThrownBy(() -> commentService.deleteComment(1L, 999999L, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("댓글을 찾을 수 없거나 삭제 권한이 없습니다. commentId: 999999");
        }
    }
}
