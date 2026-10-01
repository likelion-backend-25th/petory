package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.MissingPetStatus;
import net.likelion.bebc25.projectpatory.dto.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Test
    @DisplayName("로그인한 회원이 실종 신고 게시글을 등록한다.")
    void createMissingPet() {

        // given
        Long memberId = 103L;

        MissingPetCreateRequest request =
                new MissingPetCreateRequest(
                        LocalDate.of(2026, 9, 30),
                        "경기도 성남시 수정구 태평동",
                        "갈색 푸들이고 빨간 목줄을 착용하고 있습니다.",
                        "https://s3.example.com/missing/test-dog.jpg",
                        new BigDecimal("37.4501234"),
                        new BigDecimal("127.1405678")
                );


        // when
        Long postId =
                missingPetService.createMissingPet(
                        memberId,
                        request
                );


        // then
        assertThat(postId)
                .isNotNull();
    }


    @Test
    @DisplayName("실종 신고 등록 시 입력값과 기본 상태 MISSING이 정상적으로 저장된다.")
    void createMissingPetAndFindDetail() {

        // given
        Long memberId = 103L;

        MissingPetCreateRequest request =
                new MissingPetCreateRequest(
                        LocalDate.of(2026, 9, 30),
                        "경기도 성남시 수정구 태평동",
                        "갈색 푸들이고 빨간 목줄을 착용하고 있습니다.",
                        "https://s3.example.com/missing/test-dog.jpg",
                        new BigDecimal("37.4501234"),
                        new BigDecimal("127.1405678")
                );


        // when
        Long postId =
                missingPetService.createMissingPet(
                        memberId,
                        request
                );

        MissingPetDetailResponse response =
                missingPetService.getMissingPetDetail(postId);


        // then
        assertThat(response.id())
                .isEqualTo(postId);

        assertThat(response.author().id())
                .isEqualTo(103L);

        assertThat(response.author().nickname())
                .isEqualTo("코코");

        assertThat(response.missingDate())
                .isEqualTo(LocalDate.of(2026, 9, 30));

        assertThat(response.missingAddress())
                .isEqualTo("경기도 성남시 수정구 태평동");

        assertThat(response.detail())
                .isEqualTo("갈색 푸들이고 빨간 목줄을 착용하고 있습니다.");

        assertThat(response.imageUrl())
                .isEqualTo("https://s3.example.com/missing/test-dog.jpg");

        assertThat(response.status())
                .isEqualTo("MISSING");

        assertThat(response.latitude())
                .isEqualByComparingTo(
                        new BigDecimal("37.4501234")
                );

        assertThat(response.longitude())
                .isEqualByComparingTo(
                        new BigDecimal("127.1405678")
                );

        assertThat(response.reports())
                .isEmpty();
    }

    @Test
    @DisplayName("작성자는 자신의 실종 신고 게시글을 수정할 수 있다.")
    void updateMissingPet() {

        // given
        Long memberId = 103L;
        Long postId = 100L;

        MissingPetUpdateRequest request =
                new MissingPetUpdateRequest(
                        LocalDate.of(2026, 9, 30),
                        "경기도 성남시 수정구 태평동",
                        "수정된 특이사항입니다.",
                        "https://s3.example.com/missing/updated.jpg",
                        new BigDecimal("37.4501234"),
                        new BigDecimal("127.1405678")
                );


        // when
        missingPetService.updateMissingPet(
                memberId,
                postId,
                request
        );


        // then
        MissingPetDetailResponse response =
                missingPetService.getMissingPetDetail(postId);

        assertThat(response.id())
                .isEqualTo(100L);

        assertThat(response.author().id())
                .isEqualTo(103L);

        assertThat(response.missingDate())
                .isEqualTo(LocalDate.of(2026, 9, 30));

        assertThat(response.missingAddress())
                .isEqualTo("경기도 성남시 수정구 태평동");

        assertThat(response.detail())
                .isEqualTo("수정된 특이사항입니다.");

        assertThat(response.imageUrl())
                .isEqualTo("https://s3.example.com/missing/updated.jpg");

        assertThat(response.latitude())
                .isEqualByComparingTo(
                        new BigDecimal("37.4501234")
                );

        assertThat(response.longitude())
                .isEqualByComparingTo(
                        new BigDecimal("127.1405678")
                );

        // 일반 수정으로 status가 바뀌면 안 됨
        assertThat(response.status())
                .isEqualTo("MISSING");
    }


    @Test
    @DisplayName("작성자가 아닌 회원은 실종 신고 게시글을 수정할 수 없다.")
    void updateMissingPet_notAuthor() {

        // given
        Long memberId = 104L;
        Long postId = 100L;

        MissingPetUpdateRequest request =
                new MissingPetUpdateRequest(
                        LocalDate.of(2026, 9, 30),
                        "수정 주소",
                        "수정 내용",
                        "updated.jpg",
                        new BigDecimal("37.4500000"),
                        new BigDecimal("127.1400000")
                );


        // when & then
        assertThatThrownBy(() ->
                missingPetService.updateMissingPet(
                        memberId,
                        postId,
                        request
                )
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("실종 신고 게시글을 수정할 권한이 없습니다.");
    }


    @Test
    @DisplayName("작성자는 MISSING 상태를 FOUND로 변경할 수 있다.")
    void updateMissingPetStatus_found() {

        // given
        Long memberId = 103L;
        Long postId = 100L;

        MissingPetStatusUpdateRequest request =
                new MissingPetStatusUpdateRequest(
                        MissingPetStatus.FOUND
                );


        // when
        missingPetService.updateMissingPetStatus(
                memberId,
                postId,
                request
        );


        // then
        MissingPetDetailResponse response =
                missingPetService.getMissingPetDetail(postId);

        assertThat(response.status())
                .isEqualTo("FOUND");
    }


    @Test
    @DisplayName("작성자는 MISSING 상태를 CANCELLED로 변경할 수 있다.")
    void updateMissingPetStatus_cancelled() {

        // given
        Long memberId = 103L;
        Long postId = 100L;

        MissingPetStatusUpdateRequest request =
                new MissingPetStatusUpdateRequest(
                        MissingPetStatus.CANCELLED
                );


        // when
        missingPetService.updateMissingPetStatus(
                memberId,
                postId,
                request
        );


        // then
        MissingPetDetailResponse response =
                missingPetService.getMissingPetDetail(postId);

        assertThat(response.status())
                .isEqualTo("CANCELLED");
    }


    @Test
    @DisplayName("작성자가 아닌 회원은 실종 신고 상태를 변경할 수 없다.")
    void updateMissingPetStatus_notAuthor() {

        // given
        Long memberId = 104L;
        Long postId = 100L;

        MissingPetStatusUpdateRequest request =
                new MissingPetStatusUpdateRequest(
                        MissingPetStatus.FOUND
                );


        // when & then
        assertThatThrownBy(() ->
                missingPetService.updateMissingPetStatus(
                        memberId,
                        postId,
                        request
                )
        )
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("실종 신고 상태를 변경할 권한이 없습니다.");
    }


    @Test
    @DisplayName("이미 FOUND 상태인 게시글은 다시 상태를 변경할 수 없다.")
    void updateMissingPetStatus_alreadyFound() {

        // given
        Long memberId = 105L;
        Long postId = 101L;

        MissingPetStatusUpdateRequest request =
                new MissingPetStatusUpdateRequest(
                        MissingPetStatus.CANCELLED
                );


        // when & then
        assertThatThrownBy(() ->
                missingPetService.updateMissingPetStatus(
                        memberId,
                        postId,
                        request
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("실종중인 게시글만 상태를 변경할 수 있습니다.");
    }
}