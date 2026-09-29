package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.MissingPetListPageResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetListResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

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
        int size = 3;

        // when
        MissingPetListPageResponse response =
                missingPetService.getMissingPetList(cursor, size);

        // then
        assertThat(response.totalCount())
                .isEqualTo(5L);

        assertThat(response.items())
                .hasSize(3);

        assertThat(response.items())
                .extracting(MissingPetListResponse::id)
                .containsExactly(
                        9005L,
                        9004L,
                        9003L
                );

        assertThat(response.hasNext())
                .isTrue();

        assertThat(response.nextCursor())
                .isEqualTo(9003L);
    }


    @Test
    @DisplayName("cursor 이후 마지막 데이터를 조회하면 hasNext는 false이다.")
    void getMissingPetList_last() {

        // given
        Long cursor = 9003L;
        int size = 3;

        // when
        MissingPetListPageResponse response =
                missingPetService.getMissingPetList(cursor, size);

        // then
        assertThat(response.totalCount())
                .isEqualTo(5L);

        assertThat(response.items())
                .hasSize(2);

        assertThat(response.items())
                .extracting(MissingPetListResponse::id)
                .containsExactly(
                        9002L,
                        9001L
                );

        assertThat(response.hasNext())
                .isFalse();

        assertThat(response.nextCursor())
                .isNull();
    }


    @Test
    @DisplayName("목록 조회 시 이미지 URL이 정상적으로 반환된다.")
    void getMissingPetList_imageUrl() {

        // given
        Long cursor = null;
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
                .isEqualTo(9005L);

        assertThat(item.imageUrl())
                .isEqualTo("pet5.jpg");

        assertThat(response.hasNext())
                .isTrue();

        assertThat(response.nextCursor())
                .isEqualTo(9005L);
    }


    @Test
    @DisplayName("전체 데이터 개수보다 size가 크면 모든 데이터를 반환하고 hasNext는 false이다.")
    void getMissingPetList_noMoreData() {

        // given
        Long cursor = null;
        int size = 10;

        // when
        MissingPetListPageResponse response =
                missingPetService.getMissingPetList(cursor, size);

        // then
        assertThat(response.totalCount())
                .isEqualTo(5L);

        assertThat(response.items())
                .hasSize(5);

        assertThat(response.items())
                .extracting(MissingPetListResponse::id)
                .containsExactly(
                        9005L,
                        9004L,
                        9003L,
                        9002L,
                        9001L
                );

        assertThat(response.hasNext())
                .isFalse();

        assertThat(response.nextCursor())
                .isNull();
    }
}