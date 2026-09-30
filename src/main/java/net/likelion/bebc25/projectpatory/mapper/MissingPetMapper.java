package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.MissingPetDetailRow;
import net.likelion.bebc25.projectpatory.dto.MissingPetListResponse;
import net.likelion.bebc25.projectpatory.dto.MissingPetReportRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MissingPetMapper {
    // 목록 조회
    List<MissingPetListResponse> findByCursor(
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );

    Long countAll();

    // 분실동물 상세 게시글
    MissingPetDetailRow findDetailById(
            @Param("id") Long id
    );


    // 해당 게시글 목격 제보 목록
    List<MissingPetReportRow> findReportsByPostId(
            @Param("missingPetPostId") Long missingPetPostId
    );
}