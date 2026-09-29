package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.*;
import net.likelion.bebc25.projectpatory.mapper.MissingPetMapper;
import org.springframework.stereotype.Service;

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
}