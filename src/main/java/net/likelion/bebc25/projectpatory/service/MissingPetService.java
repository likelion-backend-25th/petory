package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.*;

public interface MissingPetService {

    // 목록 조회
    MissingPetListPageResponse getMissingPetList(
            Long cursor,
            int size
    );

    // 상세 조회
    MissingPetDetailResponse getMissingPetDetail(Long id);

    // 실종 신고 등록
    Long createMissingPet(
            Long memberId,
            MissingPetCreateRequest request
    );

    // 실종 신고 수정
    void updateMissingPet(
            Long memberId,
            Long id,
            MissingPetUpdateRequest request
    );

    // 실종 상태 변경
    void updateMissingPetStatus(
            Long memberId,
            Long id,
            MissingPetStatusUpdateRequest request
    );
}