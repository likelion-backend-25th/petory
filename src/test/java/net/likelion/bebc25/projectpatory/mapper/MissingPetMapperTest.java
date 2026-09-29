package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.MissingPetListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

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
                        9005L,
                        9004L,
                        9003L
                );
    }


    @Test
    @DisplayName("cursor가 있으면 cursor보다 작은 게시글만 조회한다.")
    void findByCursor_next() {

        // given
        Long cursor = 9004L;
        int limit = 3;

        // when
        List<MissingPetListResponse> result =
                missingPetMapper.findByCursor(cursor, limit);

        // then
        assertThat(result).hasSize(3);

        assertThat(result)
                .extracting(MissingPetListResponse::id)
                .containsExactly(
                        9003L,
                        9002L,
                        9001L
                );
    }


    @Test
    @DisplayName("목록 조회 시 이미지 URL이 정상적으로 매핑된다.")
    void findByCursor_imageUrl() {

        // given
        Long cursor = null;
        int limit = 1;

        // when
        List<MissingPetListResponse> result =
                missingPetMapper.findByCursor(cursor, limit);

        // then
        assertThat(result).hasSize(1);

        MissingPetListResponse response = result.get(0);

        assertThat(response.id())
                .isEqualTo(9005L);

        assertThat(response.imageUrl())
                .isEqualTo("pet5.jpg");
    }


    @Test
    @DisplayName("전체 분실동물 게시글 개수를 조회한다.")
    void countAll() {

        // when
        Long count = missingPetMapper.countAll();

        // then
        assertThat(count)
                .isEqualTo(5L);
    }
}