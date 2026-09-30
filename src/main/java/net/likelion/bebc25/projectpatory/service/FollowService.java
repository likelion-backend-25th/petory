package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.FollowMemberResponse;
import net.likelion.bebc25.projectpatory.dto.FollowToggleResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;

public interface FollowService {
    FollowToggleResponse toggleFollow(Long targetMemberId, Long loginMemberId);

    SliceResponse<FollowMemberResponse> getFollowers(Long memberId, Long loginMemberId, Long lastFollowId, int size);

    SliceResponse<FollowMemberResponse> getFollowings(Long memberId, Long loginMemberId, Long lastFollowId, int size);
}