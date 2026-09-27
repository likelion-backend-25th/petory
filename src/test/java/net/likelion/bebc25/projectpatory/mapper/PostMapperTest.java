package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.PostCreateRequest;
import net.likelion.bebc25.projectpatory.dto.PostUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // @MyBatisTest 대신 SpringBootTest 사용
@Transactional // 테스트 완료 후 자동 ROLLBACK
class PostMapperTest {

    @Autowired
    private PostMapper postMapper;

    // 1. 게시글 생성 Mapper 테스트
    @Test
    @DisplayName("insertPost - 정상적으로 게시글이 저장되고 생성된 PK(id)를 가져온다")
    void insertPost_Success() {
        // given
        PostCreateRequest request = PostCreateRequest.builder()
                .memberId(1L)
                .content("Mapper 테스트용 게시글입니다.")
                .bgmUrl("https://example.com/test.mp3")
                .isSubscriberOnly(0)
                .hashtags("#매퍼테스트")
                .build();

        // when
        postMapper.insertPost(request);

        // then
        assertThat(request.getId()).isNotNull(); // useGeneratedKeys로 id 세팅 확인
        assertThat(request.getId()).isGreaterThan(0L);
    }

    // 2. 게시글 수정 Mapper 테스트
    @Nested
    @DisplayName("updatePost 테스트")
    class UpdatePostTest {

        @Test
        @DisplayName("성공 - 존재하는 postId와 작성자 memberId로 수정 시 1행이 수정된다")
        void updatePost_Success() {
            // given (우선 테스트용 게시글 1개 저장)
            PostCreateRequest createReq = PostCreateRequest.builder()
                    .memberId(1L)
                    .content("수정 전 원본 내용")
                    .build();
            postMapper.insertPost(createReq);
            Long savedPostId = createReq.getId();

            PostUpdateRequest updateReq = PostUpdateRequest.builder()
                    .content("수정 후 내용")
                    .bgmUrl("https://example.com/new.mp3")
                    .isSubscriberOnly(1)
                    .hashtags("#수정완료")
                    .build();

            // when
            int updatedRows = postMapper.updatePost(savedPostId, 1L, updateReq);

            // then
            assertThat(updatedRows).isEqualTo(1);
        }

        @Test
        @DisplayName("실패 - 작성자 memberId가 다르면 0행이 수정된다")
        void updatePost_WrongMemberId_ReturnsZero() {
            // given
            PostCreateRequest createReq = PostCreateRequest.builder()
                    .memberId(1L)
                    .content("수정 전 원본 내용")
                    .build();
            postMapper.insertPost(createReq);
            Long savedPostId = createReq.getId();

            PostUpdateRequest updateReq = PostUpdateRequest.builder()
                    .content("수정 시도")
                    .build();

            // when (다른 작성자 ID인 999L로 수정 시도)
            int updatedRows = postMapper.updatePost(savedPostId, 999L, updateReq);

            // then
            assertThat(updatedRows).isEqualTo(0);
        }
    }

    // 3. 게시글 삭제 Mapper 테스트
    @Nested
    @DisplayName("deletePost 테스트")
    class DeletePostTest {

        @Test
        @DisplayName("성공 - 존재하는 postId와 작성자 memberId로 삭제 시 1행이 삭제된다")
        void deletePost_Success() {
            // given
            PostCreateRequest createReq = PostCreateRequest.builder()
                    .memberId(1L)
                    .content("삭제할 게시글")
                    .build();
            postMapper.insertPost(createReq);
            Long savedPostId = createReq.getId();

            // when
            int deletedRows = postMapper.deletePost(savedPostId, 1L);

            // then
            assertThat(deletedRows).isEqualTo(1);
        }

        @Test
        @DisplayName("실패 - 작성자 memberId가 다르면 0행이 삭제된다")
        void deletePost_WrongMemberId_ReturnsZero() {
            // given
            PostCreateRequest createReq = PostCreateRequest.builder()
                    .memberId(1L)
                    .content("삭제할 게시글")
                    .build();
            postMapper.insertPost(createReq);
            Long savedPostId = createReq.getId();

            // when (다른 작성자 ID인 999L로 삭제 시도)
            int deletedRows = postMapper.deletePost(savedPostId, 999L);

            // then
            assertThat(deletedRows).isEqualTo(0);
        }
    }
}