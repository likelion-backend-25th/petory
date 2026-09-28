package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.CommentCreateRequest;
import net.likelion.bebc25.projectpatory.dto.CommentResponse;
import net.likelion.bebc25.projectpatory.dto.PostDetailResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Test
    @DisplayName("댓글 작성 성공 - 생성된 댓글이 게시글 상세에 포함된다")
    void createComment_Success() {
        CommentCreateRequest request = CommentCreateRequest.builder()
                .content("좋은 글 잘 읽었습니다!")
                .build();

        CommentResponse created = commentService.createComment(1L, 2L, request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCommenterId()).isEqualTo(2L);
        assertThat(created.getCommenterNickname()).isEqualTo("멍치");
        assertThat(created.getContent()).isEqualTo("좋은 글 잘 읽었습니다!");
        assertThat(created.getCreatedAt()).isNotNull();

        PostDetailResponse detail = postService.getPostDetail(1L, null);
        assertThat(detail.getComments()).hasSize(3);
        assertThat(detail.getComments())
                .extracting(CommentResponse::getContent)
                .contains("좋은 글 잘 읽었습니다!");
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 댓글 작성 시 NoSuchElementException")
    void createComment_PostNotFound() {
        CommentCreateRequest request = CommentCreateRequest.builder()
                .content("없는 글에 댓글")
                .build();

        assertThatThrownBy(() -> commentService.createComment(999999L, 2L, request))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("해당 ID의 게시글을 찾을 수 없습니다. id=999999");
    }
}
