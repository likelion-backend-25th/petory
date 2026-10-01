package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PetRankingResponse;
import net.likelion.bebc25.projectpatory.dto.RankingSliceResponse;
import net.likelion.bebc25.projectpatory.mapper.RankingMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RankingServiceImplTest {

    @Mock
    private RankingMapper rankingMapper;

    @InjectMocks
    private RankingServiceImpl rankingService;

    @Test
    @DisplayName("size보다 많이 조회되면 hasNext=true이고 size개만 반환한다")
    void getRanking_whenHasNext_returnsSlicedResult() {
        // given
        List<PetRankingResponse> rows = new ArrayList<>();
        for (long i = 6; i >= 1; i--) {
            rows.add(PetRankingResponse.builder()
                    .memberId(i)
                    .nickname("펫" + i)
                    .profileImage("img" + i + ".png")
                    .followerCount(i * 10)
                    .build());
        }
        given(rankingMapper.selectByFollowerCount(null, null, 6)).willReturn(rows);

        // when
        RankingSliceResponse response = rankingService.getRanking(null, null, 5);

        // then
        assertThat(response.getContent()).hasSize(5);
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getLastMemberId()).isEqualTo(2L);
        assertThat(response.getLastFollowerCount()).isEqualTo(20L);
        verify(rankingMapper).selectByFollowerCount(null, null, 6);
    }

    @Test
    @DisplayName("size 이하로 조회되면 hasNext=false이다")
    void getRanking_whenNoNext_returnsHasNextFalse() {
        // given
        List<PetRankingResponse> rows = List.of(
                PetRankingResponse.builder().memberId(3L).nickname("A").followerCount(30).build(),
                PetRankingResponse.builder().memberId(2L).nickname("B").followerCount(20).build()
        );
        given(rankingMapper.selectByFollowerCount(null, null, 6)).willReturn(new ArrayList<>(rows));

        // when
        RankingSliceResponse response = rankingService.getRanking(null, null, 5);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getLastMemberId()).isEqualTo(2L);
        assertThat(response.getLastFollowerCount()).isEqualTo(20L);
    }

    @Test
    @DisplayName("커서가 모두 있으면 매퍼에 그대로 전달한다")
    void getRanking_whenCursorPresent_passesCursorToMapper() {
        // given
        given(rankingMapper.selectByFollowerCount(100L, 3L, 21)).willReturn(new ArrayList<>());

        // when
        RankingSliceResponse response = rankingService.getRanking(100L, 3L, 20);

        // then
        assertThat(response.getContent()).isEmpty();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getLastMemberId()).isNull();
        assertThat(response.getLastFollowerCount()).isNull();
        verify(rankingMapper).selectByFollowerCount(100L, 3L, 21);
    }

    @Test
    @DisplayName("커서가 한쪽만 있으면 첫 페이지로 취급한다")
    void getRanking_whenPartialCursor_treatsAsFirstPage() {
        // given
        given(rankingMapper.selectByFollowerCount(null, null, 21)).willReturn(new ArrayList<>());

        // when
        rankingService.getRanking(100L, null, 20);

        // then
        verify(rankingMapper).selectByFollowerCount(null, null, 21);
    }
}
