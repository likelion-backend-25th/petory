package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.MissingPetDetailResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetListPageResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetListResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetReportResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Sql("classpath:/sql/missing-pet-test-data.sql")
class MissingPetServiceTest {

    @Autowired
    private MissingPetService missingPetService;


    @Test
    @DisplayName("첫 조회 시 size만큼 반환하고 다음 데이터가 있으면 hasNext는 true이다.")
    void getMissingPetList_first() {

        // given
        Long cursor = null;
        int size = 2;

        // when
        MissingPetListPageResponse response =
                missingPetService.getMissingPetList(cursor, size);

        // then
        assertThat(response.totalCount())
                .isEqualTo(6L);

        assertThat(response.items())
                .hasSize(2);

        assertThat(response.items())
                .extracting(MissingPetListResponse::id)
                .containsExactly(
                        102L,
                        101L
                );

        assertThat(response.hasNext())
                .isTrue();

        assertThat(response.nextCursor())
                .isEqualTo(101L);
    }


    @Test
    @DisplayName("목록 조회 시 이미지 URL이 정상적으로 반환된다.")
    void getMissingPetList_imageUrl() {

        // given
        Long cursor = 102L;
        int size = 1;

        // when
        MissingPetListPageResponse response =
                missingPetService.getMissingPetList(cursor, size);

        // then
        assertThat(response.items())
                .hasSize(1);

        MissingPetListResponse item =
                response.items().get(0);

        assertThat(item.id())
                .isEqualTo(101L);

        assertThat(item.imageUrl())
                .isEqualTo("https://s3.example.com/missing/2.jpg");

        assertThat(response.hasNext())
                .isTrue();

        assertThat(response.nextCursor())
                .isEqualTo(101L);
    }


    @Test
    @DisplayName("분실동물 게시글 상세 정보와 목격 제보 목록을 조회한다.")
    void getMissingPetDetail() {

        // given
        Long postId = 100L;

        // when
        MissingPetDetailResponse response =
                missingPetService.getMissingPetDetail(postId);

        // then
        assertThat(response).isNotNull();


        // 게시글 정보
        assertThat(response.id())
                .isEqualTo(100L);

        assertThat(response.missingDate())
                .isEqualTo(LocalDate.of(2025, 7, 8));

        assertThat(response.missingAddress())
                .isEqualTo("경기도 성남시 분당구 정자동 공원 근처");

        assertThat(response.detail())
                .isEqualTo("흰색 푸들, 빨간 목걸이 착용. 이름이 코코입니다.");

        assertThat(response.imageUrl())
                .isEqualTo("https://s3.example.com/missing/1.jpg");

        assertThat(response.status())
                .isEqualTo("MISSING");

        assertThat(response.latitude())
                .isEqualByComparingTo("37.3595000");

        assertThat(response.longitude())
                .isEqualByComparingTo("127.1052000");


        // 게시글 작성자
        assertThat(response.author())
                .isNotNull();

        assertThat(response.author().id())
                .isEqualTo(103L);

        assertThat(response.author().nickname())
                .isEqualTo("코코");

        assertThat(response.author().profileImage())
                .isEqualTo("https://s3.example.com/profile/coco.jpg");


        // 목격 제보 목록
        assertThat(response.reports())
                .hasSize(1);

        MissingPetReportResponse report =
                response.reports().get(0);


        // 목격 제보 정보
        assertThat(report.id())
                .isEqualTo(100L);

        assertThat(report.address())
                .isEqualTo("성남시 분당구 수내동 카페거리");

        assertThat(report.detail())
                .isEqualTo(
                        "빨간 목걸이 흰 푸들 비슷한 아이를 봤어요. 사람 가까이 오진 않았습니다."
                );

        assertThat(report.imageUrl())
                .isEqualTo("https://s3.example.com/report/1.jpg");

        assertThat(report.sightAt())
                .isEqualTo(
                        LocalDateTime.of(
                                2025,
                                7,
                                9,
                                8,
                                30
                        )
                );


        // 목격 제보 작성자
        assertThat(report.reporter())
                .isNotNull();

        assertThat(report.reporter().id())
                .isEqualTo(101L);

        assertThat(report.reporter().nickname())
                .isEqualTo("멍치");

        assertThat(report.reporter().profileImage())
                .isEqualTo("https://s3.example.com/profile/mungchi.jpg");
    }


    @Test
    @DisplayName("존재하지 않는 분실동물 게시글을 조회하면 예외가 발생한다.")
    void getMissingPetDetail_notFound() {

        // given
        Long postId = 999999L;

        // when & then
        assertThatThrownBy(
                () -> missingPetService.getMissingPetDetail(postId)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 실종 신고 게시글입니다.");
    }
}