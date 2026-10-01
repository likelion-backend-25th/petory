package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.PetRankingResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RankingMapper {

    // 팔로워 수 기반 랭킹 리스트
    List<PetRankingResponse> selectByFollowerCount(
            @Param("lastFollowerCount") Long lastFollowerCount,
            @Param("lastMemberId") Long lastMemberId,
            @Param("limit") int limit);
}
