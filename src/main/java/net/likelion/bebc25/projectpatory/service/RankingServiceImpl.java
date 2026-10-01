package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.PetRankingResponse;
import net.likelion.bebc25.projectpatory.dto.RankingSliceResponse;
import net.likelion.bebc25.projectpatory.mapper.RankingMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;

    @Override
    public RankingSliceResponse getRanking(Long lastFollowerCount, Long lastMemberId, int size) {
        // lastFollowerCount와 lastMemberId가 정확히 명시되어야 다음 커서 시작시점 설정
        Long cursorFollowerCount = (lastFollowerCount != null && lastMemberId != null) ? lastFollowerCount : null;
        Long cursorMemberId = (lastFollowerCount != null && lastMemberId != null) ? lastMemberId : null;

        // 다음 페이지 조회
        List<PetRankingResponse> list =
                rankingMapper.selectByFollowerCount(cursorFollowerCount, cursorMemberId, size + 1);

        boolean hasNext = false;
        if (list.size() > size) {
            hasNext = true;
            list.remove(size);
        }

        // 다음 커서 생성
        Long nextMemberId = null;
        Long nextFollowerCount = null;
        if (!list.isEmpty()) {
            PetRankingResponse last = list.get(list.size() - 1);
            nextMemberId = last.getMemberId();
            nextFollowerCount = last.getFollowerCount();
        }

        return new RankingSliceResponse(list, hasNext, nextMemberId, nextFollowerCount);
    }
}
