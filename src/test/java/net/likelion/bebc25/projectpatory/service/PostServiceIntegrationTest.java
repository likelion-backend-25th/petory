package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PostDetailResponse;
import net.likelion.bebc25.projectpatory.dto.PostListResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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


    @Test
    @DisplayName("Docker DB 연동 - 게시글 상세 조회 성공 (member 테이블 JOIN 정보 검증)")
    void getPostDetail_Success_DockerDb() {
        // given: Docker DB의 post_main 테이블에 존재하는 게시글 ID (예: 1L)
        Long targetPostId = 1L;

        // when: MyBatis 단건 JOIN 쿼리 실행
        PostDetailResponse response = postService.getPostDetail(targetPostId);

        // then: 기본 데이터 및 member 테이블과 조인된 작성자 정보 출력 및 검증
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(targetPostId);
        assertThat(response.getAuthorName()).isNotNull(); // member.nickname 조인 검증

        System.out.println("=========================================");
        System.out.println("게시글 상세 조회 성공 - ID: " + response.getId());
        System.out.println("본문 내용: " + response.getContent());
        System.out.println("작성자 ID: " + response.getMemberId());
        System.out.println("작성자 닉네임: " + response.getAuthorName());
        System.out.println("작성자 프로필 이미지: " + response.getAuthorProfileImage());
        System.out.println("=========================================");
    }

    @Test
    @DisplayName("Docker DB 연동 - 존재하지 않는 게시글 ID 조회 시 NoSuchElementException 발생")
    void getPostDetail_NotFound_DockerDb() {
        // given: DB에 절대로 존재하지 않을 큰 ID 값
        Long notFoundPostId = 999999L;

        // when & then: 예외 발생 및 메시지 검증
        assertThatThrownBy(() -> postService.getPostDetail(notFoundPostId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("해당 ID의 게시글을 찾을 수 없습니다. id=" + notFoundPostId);
    }
}