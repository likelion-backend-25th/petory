package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.FollowMemberResponse;
import net.likelion.bebc25.projectpatory.dto.FollowToggleResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import net.likelion.bebc25.projectpatory.mapper.FollowMapper;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;
    private final MemberMapper memberMapper;

    @Override
    @Transactional
    public FollowToggleResponse toggleFollow(Long targetMemberId, Long loginMemberId) {
        // 자기 자신 팔로우 시도시 예외처리
        if (loginMemberId.equals(targetMemberId)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }
        // 존재하지 않는 회원 팔로우 시도시 예외처리
        if (memberMapper.findById(targetMemberId) == null) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }

        boolean following;
        // 팔로잉 상태에서 팔로우 취소시 / 언팔로우 상태에서 팔로우 시
        if (followMapper.existsFollow(loginMemberId, targetMemberId)) {
            followMapper.deleteFollow(loginMemberId, targetMemberId);
            following = false;
        } else {
            followMapper.insertFollow(loginMemberId, targetMemberId);
            following = true;
        }

        return FollowToggleResponse.builder()
                .following(following)
                .followerCount(followMapper.countFollowers(targetMemberId))
                .build();
    }

    // 팔로워 목록 분할
    @Override
    public SliceResponse<FollowMemberResponse> getFollowers(Long memberId, Long loginMemberId,
                                                            Long lastFollowId, int size) {
        if (memberMapper.findById(memberId) == null) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }
        return toSlice(followMapper.selectFollowers(memberId, loginMemberId, lastFollowId, size + 1), size);
    }

    // 팔로잉 목록 분할
    @Override
    public SliceResponse<FollowMemberResponse> getFollowings(Long memberId, Long loginMemberId,
                                                             Long lastFollowId, int size) {
        if (memberMapper.findById(memberId) == null) {
            throw new NoSuchElementException("존재하지 않는 회원입니다.");
        }
        return toSlice(followMapper.selectFollowings(memberId, loginMemberId, lastFollowId, size + 1), size);
    }

    private SliceResponse<FollowMemberResponse> toSlice(List<FollowMemberResponse> list, int size) {
        boolean hasNext = false;
        if (list.size() > size) {
            hasNext = true;
            list.remove(size);
        }
        Long nextCursor = list.isEmpty() ? null : list.get(list.size() - 1).getFollowId();
        return new SliceResponse<>(list, hasNext, nextCursor);
    }
}