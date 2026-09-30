package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.domain.MissingPetPost;
import net.likelion.bebc25.projectpatory.domain.MissingPetStatus;
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

    // 실종 신고 등록
    int insertMissingPet(MissingPetPost missingPetPost);

    // 게시글 작성자 ID 조회
    Long findMemberIdByPostId(
            @Param("id") Long id
    );

    // 실종 신고 게시글 수정
    int updateMissingPet(MissingPetPost missingPetPost);

    // 현재 게시글 상태 조회
    String findStatusByPostId(
            @Param("id") Long id
    );

    // 실종 신고 상태 변경
    int updateMissingPetStatus(
            @Param("id") Long id,
            @Param("status") MissingPetStatus status
    );
}