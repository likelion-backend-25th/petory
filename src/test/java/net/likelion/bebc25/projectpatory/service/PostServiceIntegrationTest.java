package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PostListResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // application.yml 설정을 그대로 읽어 Docker DB와 스프링 컨테이너를 구동함
@Transactional // 테스트 중 수행된 CUD 작업이 DB에 안 남도록 자동 롤백
class PostServiceIntegrationTest {

    @Autowired
    private PostService postService; // 진짜 서비스 빈 주입

    @Test
    @DisplayName("Docker DB 연동 - 메인 피드 무한 스크롤 첫 페이지 조회")
    void getPostListCursor_FirstPage_DockerDb() {
        // given: 첫 조회 시 lastPostId = null, 한 번에 10개
        Long lastPostId = null;
        int size = 10;

        // when: Docker의 MySQL DB로 진짜 SQL 쿼리가 날아감
        SliceResponse<PostListResponse> response = postService.getPostListCursor(lastPostId, size);

        // then: 결과 출력 및 기본 검증
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isNotNull();

        System.out.println("=========================================");
        System.out.println("조회된 게시글 개수: " + response.getContent().size());
        System.out.println("다음 페이지 존재 여부(hasNext): " + response.getHasNext());
        System.out.println("다음 커서 ID(lastPostId): " + response.getLastPostId());

        response.getContent().forEach(post -> {
            System.out.println("게시글 ID: " + post.getId()
                    + " | 작성자: " + post.getNickname()
                    + " | 내용: " + post.getContent());
        });
        System.out.println("=========================================");
    }

    @Test
    @DisplayName("Docker DB 연동 - 커서 기반 다음 페이지 조회 테스트")
    void getPostListCursor_NextPage_DockerDb() {
        // given: 특정 게시글 ID 기준 (예: ID 20번 이전 글 조회)
        Long lastPostId = 20L;
        int size = 5;

        // when
        SliceResponse<PostListResponse> response = postService.getPostListCursor(lastPostId, size);

        // then
        assertThat(response).isNotNull();

        // 가져온 게시글 중 lastPostId(20)보다 크거나 같은 ID가 없는지 검증
        if (!response.getContent().isEmpty()) {
            assertThat(response.getContent().get(0).getId()).isLessThan(lastPostId);
        }
    }
}