package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.FollowMemberResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {

    // 상대 팔로우 여부 확인
    boolean existsFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    // 팔로우 등록
    void insertFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    // 팔로우 취소
    int deleteFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    // 팔로워 수
    long countFollowers(@Param("memberId") Long memberId);

    // 팔로잉 수
    long countFollowings(@Param("memberId") Long memberId);

    // 팔로워 리스트
    List<FollowMemberResponse> selectFollowers(@Param("memberId") Long memberId,
                                               @Param("loginMemberId") Long loginMemberId,
                                               @Param("lastFollowId") Long lastFollowId,
                                               @Param("limit") int limit);

    // 팔로잉 리스트
    List<FollowMemberResponse> selectFollowings(@Param("memberId") Long memberId,
                                                @Param("loginMemberId") Long loginMemberId,
                                                @Param("lastFollowId") Long lastFollowId,
                                                @Param("limit") int limit);
}
