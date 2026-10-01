package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.MissingPetPost;
import net.likelion.bebc25.projectpatory.domain.MissingPetStatus;
import net.likelion.bebc25.projectpatory.dto.MissingPetDetailRow;
import net.likelion.bebc25.projectpatory.dto.MissingPetListResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetReportRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
                .isEqualTo(16L);
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

    @Test
    @DisplayName("실종 신고 게시글을 등록한다.")
    void insertMissingPet() {

        // given
        MissingPetPost missingPetPost =
                MissingPetPost.builder()
                        .memberId(103L)
                        .missingDate(LocalDate.of(2026, 9, 30))
                        .missingAddress("경기도 성남시 수정구 태평동")
                        .detail("갈색 푸들이고 빨간 목줄을 착용하고 있습니다.")
                        .imageUrl("https://s3.example.com/missing/test-dog.jpg")
                        .status(MissingPetStatus.MISSING)
                        .latitude(new BigDecimal("37.4501234"))
                        .longitude(new BigDecimal("127.1405678"))
                        .build();


        // when
        int result =
                missingPetMapper.insertMissingPet(missingPetPost);


        // then
        assertThat(result).isEqualTo(1);

        // useGeneratedKeys 동작 확인
        assertThat(missingPetPost.getId())
                .isNotNull();
    }


    @Test
    @DisplayName("등록한 실종 신고 게시글의 내용이 정상적으로 저장된다.")
    void insertAndFindMissingPet() {

        // given
        MissingPetPost missingPetPost =
                MissingPetPost.builder()
                        .memberId(103L)
                        .missingDate(LocalDate.of(2026, 9, 30))
                        .missingAddress("경기도 성남시 수정구 태평동")
                        .detail("갈색 푸들이고 빨간 목줄을 착용하고 있습니다.")
                        .imageUrl("https://s3.example.com/missing/test-dog.jpg")
                        .status(MissingPetStatus.MISSING)
                        .latitude(new BigDecimal("37.4501234"))
                        .longitude(new BigDecimal("127.1405678"))
                        .build();


        // when
        missingPetMapper.insertMissingPet(missingPetPost);

        MissingPetDetailRow result =
                missingPetMapper.findDetailById(
                        missingPetPost.getId()
                );


        // then
        assertThat(result).isNotNull();

        assertThat(result.id())
                .isEqualTo(missingPetPost.getId());

        assertThat(result.authorId())
                .isEqualTo(103L);

        assertThat(result.authorNickname())
                .isEqualTo("코코");

        assertThat(result.missingDate())
                .isEqualTo(LocalDate.of(2026, 9, 30));

        assertThat(result.missingAddress())
                .isEqualTo("경기도 성남시 수정구 태평동");

        assertThat(result.detail())
                .isEqualTo("갈색 푸들이고 빨간 목줄을 착용하고 있습니다.");

        assertThat(result.imageUrl())
                .isEqualTo("https://s3.example.com/missing/test-dog.jpg");

        assertThat(result.status())
                .isEqualTo("MISSING");

        assertThat(result.latitude())
                .isEqualByComparingTo(
                        new BigDecimal("37.4501234")
                );

        assertThat(result.longitude())
                .isEqualByComparingTo(
                        new BigDecimal("127.1405678")
                );
    }

    @Test
    @DisplayName("실종 신고 게시글을 수정한다.")
    void updateMissingPet() {

        // given
        MissingPetPost missingPetPost =
                MissingPetPost.builder()
                        .id(100L)
                        .memberId(103L)
                        .missingDate(LocalDate.of(2026, 9, 30))
                        .missingAddress("경기도 성남시 수정구 태평동")
                        .detail("수정된 특이사항입니다.")
                        .imageUrl("https://s3.example.com/missing/updated.jpg")
                        .latitude(new BigDecimal("37.4501234"))
                        .longitude(new BigDecimal("127.1405678"))
                        .build();


        // when
        int updatedCount =
                missingPetMapper.updateMissingPet(missingPetPost);


        // then
        assertThat(updatedCount)
                .isEqualTo(1);

        MissingPetDetailRow result =
                missingPetMapper.findDetailById(100L);

        assertThat(result.missingDate())
                .isEqualTo(LocalDate.of(2026, 9, 30));

        assertThat(result.missingAddress())
                .isEqualTo("경기도 성남시 수정구 태평동");

        assertThat(result.detail())
                .isEqualTo("수정된 특이사항입니다.");

        assertThat(result.imageUrl())
                .isEqualTo("https://s3.example.com/missing/updated.jpg");

        assertThat(result.latitude())
                .isEqualByComparingTo(
                        new BigDecimal("37.4501234")
                );

        assertThat(result.longitude())
                .isEqualByComparingTo(
                        new BigDecimal("127.1405678")
                );

        // 일반 수정에서는 status가 변경되지 않아야 함
        assertThat(result.status())
                .isEqualTo("MISSING");
    }


    @Test
    @DisplayName("실종 신고 게시글의 작성자 회원 ID를 조회한다.")
    void findMemberIdByPostId() {

        // when
        Long memberId =
                missingPetMapper.findMemberIdByPostId(100L);


        // then
        assertThat(memberId)
                .isEqualTo(103L);
    }


    @Test
    @DisplayName("실종 신고 게시글의 현재 상태를 조회한다.")
    void findStatusByPostId() {

        // when
        String status =
                missingPetMapper.findStatusByPostId(100L);


        // then
        assertThat(status)
                .isEqualTo("MISSING");
    }


    @Test
    @DisplayName("실종 신고 상태를 FOUND로 변경한다.")
    void updateMissingPetStatus_found() {

        // when
        int updatedCount =
                missingPetMapper.updateMissingPetStatus(
                        100L,
                        MissingPetStatus.FOUND
                );


        // then
        assertThat(updatedCount)
                .isEqualTo(1);

        String status =
                missingPetMapper.findStatusByPostId(100L);

        assertThat(status)
                .isEqualTo("FOUND");
    }


    @Test
    @DisplayName("실종 신고 상태를 CANCELLED로 변경한다.")
    void updateMissingPetStatus_cancelled() {

        // when
        int updatedCount =
                missingPetMapper.updateMissingPetStatus(
                        100L,
                        MissingPetStatus.CANCELLED
                );


        // then
        assertThat(updatedCount)
                .isEqualTo(1);

        String status =
                missingPetMapper.findStatusByPostId(100L);

        assertThat(status)
                .isEqualTo("CANCELLED");
    }
}