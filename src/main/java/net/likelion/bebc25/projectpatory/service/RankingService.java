package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.RankingSliceResponse;

public interface RankingService {

    RankingSliceResponse getRanking(Long lastFollowerCount, Long lastMemberId, int size);
}
