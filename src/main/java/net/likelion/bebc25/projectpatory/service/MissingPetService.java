package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.MissingPetDetailResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetListPageResponse;

public interface MissingPetService {

    // 목록 조회
    MissingPetListPageResponse getMissingPetList(
            Long cursor,
            int size
    );

    // 상세 조회
    MissingPetDetailResponse getMissingPetDetail(Long id);
}