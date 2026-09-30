package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.MissingPetDetailRow;
import net.likelion.bebc25.projectpatory.dto.MissingPetListResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetReportRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Sql("classpath:/sql/missing-pet-test-data.sql")
class MissingPetMapperTest {

    @Autowired
    private MissingPetMapper missingPetMapper;


    @Test
    @DisplayName("첫 조회 시 최신 게시글부터 limit만큼 조회한다.")
    void findByCursor_first() {

        // given
        Long cursor = null;
        int limit = 3;

        // when
        List<MissingPetListResponse> result =
                missingPetMapper.findByCursor(cursor, limit);

        // then
        assertThat(result).hasSize(3);

        assertThat(result)
                .extracting(MissingPetListResponse::id)
                .containsExactly(
                        102L,
                        101L,
                        100L
                );
    }


    @Test
    @DisplayName("cursor가 있으면 cursor보다 작은 게시글만 조회한다.")
    void findByCursor_next() {

        // given
        Long cursor = 102L;
        int limit = 2;

        // when
        List<MissingPetListResponse> result =
                missingPetMapper.findByCursor(cursor, limit);

        // then
        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(MissingPetListResponse::id)
                .containsExactly(
                        101L,
                        100L
                );
    }


    @Test
    @DisplayName("목록 조회 시 이미지 URL이 정상적으로 매핑된다.")
    void findByCursor_imageUrl() {

        // given
        Long cursor = 102L;
        int limit = 1;

        // when
        List<MissingPetListResponse> result =
                missingPetMapper.findByCursor(cursor, limit);

        // then
        assertThat(result).hasSize(1);

        MissingPetListResponse response = result.get(0);

        assertThat(response.id())
                .isEqualTo(101L);

        assertThat(response.imageUrl())
                .isEqualTo("https://s3.example.com/missing/2.jpg");
    }


    @Test
    @DisplayName("전체 분실동물 게시글 개수를 조회한다.")
    void countAll() {

        // when
        Long count = missingPetMapper.countAll();

        // then
        assertThat(count)
                .isEqualTo(6L);
    }


    @Test
    @DisplayName("게시글 ID로 분실동물 상세 게시글을 조회한다.")
    void findDetailById() {

        // given
        Long postId = 100L;

        // when
        MissingPetDetailRow result =
                missingPetMapper.findDetailById(postId);

        // then
        assertThat(result).isNotNull();

        // 게시글 정보
        assertThat(result.id())
                .isEqualTo(100L);

        assertThat(result.missingDate())
                .isEqualTo(LocalDate.of(2025, 7, 8));

        assertThat(result.missingAddress())
                .isEqualTo("경기도 성남시 분당구 정자동 공원 근처");

        assertThat(result.detail())
                .isEqualTo("흰색 푸들, 빨간 목걸이 착용. 이름이 코코입니다.");

        assertThat(result.imageUrl())
                .isEqualTo("https://s3.example.com/missing/1.jpg");

        assertThat(result.status())
                .isEqualTo("MISSING");


        // 작성자 JOIN 결과
        assertThat(result.authorId())
                .isEqualTo(103L);

        assertThat(result.authorNickname())
                .isEqualTo("코코");

        assertThat(result.authorProfileImage())
                .isEqualTo("https://s3.example.com/profile/coco.jpg");
    }


    @Test
    @DisplayName("게시글 ID로 해당 게시글의 목격 제보 목록을 조회한다.")
    void findReportsByPostId() {

        // given
        Long postId = 100L;

        // when
        List<MissingPetReportRow> result =
                missingPetMapper.findReportsByPostId(postId);

        // then
        assertThat(result).hasSize(1);

        MissingPetReportRow report = result.get(0);


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


        // 제보자 JOIN 결과
        assertThat(report.reporterId())
                .isEqualTo(101L);

        assertThat(report.reporterNickname())
                .isEqualTo("멍치");

        assertThat(report.reporterProfileImage())
                .isEqualTo("https://s3.example.com/profile/mungchi.jpg");
    }
}