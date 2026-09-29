package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.MissingPetListPageResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetListResponse;
import net.likelion.bebc25.projectpatory.mapper.MissingPetMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MissingPetServiceImpl implements MissingPetService {

    private final MissingPetMapper missingPetMapper;

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
}