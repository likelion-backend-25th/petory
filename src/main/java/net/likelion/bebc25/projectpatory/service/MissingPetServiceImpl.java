package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.domain.MissingPetPost;
import net.likelion.bebc25.projectpatory.domain.MissingPetStatus;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.mapper.MissingPetMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissingPetServiceImpl implements MissingPetService {

    private final MissingPetMapper missingPetMapper;

    // 목록 조회
    @Override
    public MissingPetListPageResponse getMissingPetList(
            Long cursor,
            int size
    ) {

        // 다음 데이터가 있는지 확인하기 위해 1개 더 조회
        List<MissingPetListResponse> result =
                missingPetMapper.findByCursor(cursor, size + 1);

        boolean hasNext = result.size() > size;

        // 실제 응답에는 size 개수만 전달
        List<MissingPetListResponse> items;

        if (hasNext) {
            items = result.subList(0, size);
        } else {
            items = result;
        }

        // 다음 요청에 사용할 cursor
        Long nextCursor = null;

        if (hasNext && !items.isEmpty()) {
            nextCursor = items.get(items.size() - 1).id();
        }

        Long totalCount = missingPetMapper.countAll();

        return new MissingPetListPageResponse(
                totalCount,
                items,
                nextCursor,
                hasNext
        );
    }

    // 상세 조회
    @Override
    public MissingPetDetailResponse getMissingPetDetail(Long id) {

        // 1. 실종 신고 게시글 + 작성자 조회
        MissingPetDetailRow detail =
                missingPetMapper.findDetailById(id);

        if (detail == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 실종 신고 게시글입니다."
            );
        }

        // 2. 작성자 정보 DTO 생성
        MemberSummaryResponse author =
                new MemberSummaryResponse(
                        detail.authorId(),
                        detail.authorNickname(),
                        detail.authorProfileImage()
                );


        // 3. 목격 제보 목록 조회
        List<MissingPetReportRow> reportRows =
                missingPetMapper.findReportsByPostId(id);


        // 4. 목격 제보 Row → Response 변환
        List<MissingPetReportResponse> reports =
                reportRows.stream()
                        .map(report -> {

                            MemberSummaryResponse reporter =
                                    new MemberSummaryResponse(
                                            report.reporterId(),
                                            report.reporterNickname(),
                                            report.reporterProfileImage()
                                    );

                            return new MissingPetReportResponse(
                                    report.id(),
                                    reporter,
                                    report.address(),
                                    report.detail(),
                                    report.imageUrl(),
                                    report.sightAt(),
                                    report.latitude(),
                                    report.longitude(),
                                    report.createdAt()
                            );
                        })
                        .toList();


        // 5. 최종 상세 응답 생성
        return new MissingPetDetailResponse(
                detail.id(),
                author,
                detail.missingDate(),
                detail.missingAddress(),
                detail.detail(),
                detail.imageUrl(),
                detail.status(),
                detail.latitude(),
                detail.longitude(),
                detail.createdAt(),
                detail.updatedAt(),
                reports
        );
    }

    // 실종 신고 등록
    @Override
    @Transactional
    public Long createMissingPet(
            Long memberId,
            MissingPetCreateRequest request
    ) {

        MissingPetPost missingPetPost =
                MissingPetPost.builder()
                        .memberId(memberId)
                        .missingDate(request.missingDate())
                        .missingAddress(request.missingAddress())
                        .detail(request.detail())
                        .imageUrl(request.imageUrl())
                        .status(MissingPetStatus.MISSING)
                        .latitude(request.latitude())
                        .longitude(request.longitude())
                        .build();

        missingPetMapper.insertMissingPet(missingPetPost);

        return missingPetPost.getId();
    }

    // 실종 신고 수정
    @Override
    @Transactional
    public void updateMissingPet(
            Long memberId,
            Long id,
            MissingPetUpdateRequest request
    ){

        // 1. 게시글 작성자 조회
        Long authorId =
                missingPetMapper.findMemberIdByPostId(id);

        // 2. 게시글 존재 여부 확인
        if (authorId == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 실종 신고 게시글입니다."
            );
        }

        // 3. 현재 로그인 회원이 작성자인지 확인
        if (!authorId.equals(memberId)) {
            throw new AccessDeniedException(
                    "실종 신고 게시글을 수정할 권한이 없습니다."
            );
        }

        // 4. 수정할 Domain 생성
        MissingPetPost missingPetPost =
                MissingPetPost.builder()
                        .id(id)
                        .memberId(memberId)
                        .missingDate(request.missingDate())
                        .missingAddress(request.missingAddress())
                        .detail(request.detail())
                        .imageUrl(request.imageUrl())
                        .latitude(request.latitude())
                        .longitude(request.longitude())
                        .build();

        // 5. DB 수정
        int updatedCount =
                missingPetMapper.updateMissingPet(missingPetPost);

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "실종 신고 게시글 수정에 실패했습니다."
            );
        }
    }

    // 실종 상태 변경
    @Override
    @Transactional
    public void updateMissingPetStatus(
            Long memberId,
            Long id,
            MissingPetStatusUpdateRequest request
    ){

        // 1. 게시글 작성자 조회
        Long authorId =
                missingPetMapper.findMemberIdByPostId(id);

        // 2. 게시글 존재 여부 확인
        if (authorId == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 실종 신고 게시글입니다."
            );
        }

        // 3. 작성자 본인인지 확인
        if (!authorId.equals(memberId)) {
            throw new AccessDeniedException(
                    "실종 신고 상태를 변경할 권한이 없습니다."
            );
        }

        // 4. FOUND / CANCELLED만 허용
        if (request.status() != MissingPetStatus.FOUND
                && request.status() != MissingPetStatus.CANCELLED) {

            throw new IllegalArgumentException(
                    "FOUND 또는 CANCELLED 상태만 선택할 수 있습니다."
            );
        }

        // 5. 현재 게시글 상태 확인
        String currentStatus =
                missingPetMapper.findStatusByPostId(id);

        if (!MissingPetStatus.MISSING.name().equals(currentStatus)) {
            throw new IllegalStateException(
                    "실종중인 게시글만 상태를 변경할 수 있습니다."
            );
        }

        // 6. 상태 변경
        int updatedCount =
                missingPetMapper.updateMissingPetStatus(
                        id,
                        request.status()
                );

        if (updatedCount != 1) {
            throw new IllegalStateException(
                    "실종 신고 상태 변경에 실패했습니다."
            );
        }
    }
}