package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.domain.Member;
import net.likelion.bebc25.projectpatory.dto.FollowMemberResponse;
import net.likelion.bebc25.projectpatory.dto.FollowToggleResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import net.likelion.bebc25.projectpatory.mapper.FollowMapper;
import net.likelion.bebc25.projectpatory.mapper.MemberMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowMapper followMapper;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private FollowServiceImpl followService;

    @Nested
    @DisplayName("팔로우 토글")
    class ToggleFollowTest {

        @Test
        @DisplayName("언팔로우 상태에서 팔로우하면 insert 후 following=true와 팔로워 수를 반환한다")
        void toggleFollow_whenNotFollowing_insertsAndReturnsFollowingTrue() {
            // given
            Long loginMemberId = 1L;
            Long targetMemberId = 2L;
            given(memberMapper.findById(targetMemberId)).willReturn(Member.builder().id(targetMemberId).build());
            given(followMapper.existsFollow(loginMemberId, targetMemberId)).willReturn(false);
            given(followMapper.countFollowers(targetMemberId)).willReturn(4L);

            // when
            FollowToggleResponse response = followService.toggleFollow(targetMemberId, loginMemberId);

            // then
            assertThat(response.isFollowing()).isTrue();
            assertThat(response.getFollowerCount()).isEqualTo(4L);
            verify(followMapper).insertFollow(loginMemberId, targetMemberId);
            verify(followMapper, never()).deleteFollow(loginMemberId, targetMemberId);
        }

        @Test
        @DisplayName("팔로우 상태에서 다시 토글하면 delete 후 following=false와 팔로워 수를 반환한다")
        void toggleFollow_whenFollowing_deletesAndReturnsFollowingFalse() {
            // given
            Long loginMemberId = 1L;
            Long targetMemberId = 2L;
            given(memberMapper.findById(targetMemberId)).willReturn(Member.builder().id(targetMemberId).build());
            given(followMapper.existsFollow(loginMemberId, targetMemberId)).willReturn(true);
            given(followMapper.countFollowers(targetMemberId)).willReturn(3L);

            // when
            FollowToggleResponse response = followService.toggleFollow(targetMemberId, loginMemberId);

            // then
            assertThat(response.isFollowing()).isFalse();
            assertThat(response.getFollowerCount()).isEqualTo(3L);
            verify(followMapper).deleteFollow(loginMemberId, targetMemberId);
            verify(followMapper, never()).insertFollow(loginMemberId, targetMemberId);
        }

        @Test
        @DisplayName("자기 자신을 팔로우하면 IllegalArgumentException을 던진다")
        void toggleFollow_whenSelf_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> followService.toggleFollow(1L, 1L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("자기 자신");

            verifyNoInteractions(followMapper);
            verify(memberMapper, never()).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 회원을 팔로우하면 NoSuchElementException을 던진다")
        void toggleFollow_whenTargetNotFound_throwsNoSuchElementException() {
            given(memberMapper.findById(99L)).willReturn(null);

            assertThatThrownBy(() -> followService.toggleFollow(99L, 1L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("존재하지 않는 회원");

            verifyNoInteractions(followMapper);
        }
    }

    @Nested
    @DisplayName("팔로워 목록")
    class GetFollowersTest {

        @Test
        @DisplayName("size보다 1개 더 조회되면 hasNext=true이고 초과분은 제거한다")
        void getFollowers_whenHasNext_returnsSlicedResult() {
            // given
            Long memberId = 2L;
            Long loginMemberId = 1L;
            given(memberMapper.findById(memberId)).willReturn(Member.builder().id(memberId).build());

            List<FollowMemberResponse> rows = new ArrayList<>();
            rows.add(followMember(10L, 3L));
            rows.add(followMember(9L, 4L));
            rows.add(followMember(8L, 5L)); // size+1
            given(followMapper.selectFollowers(memberId, loginMemberId, null, 3)).willReturn(rows);

            // when
            SliceResponse<FollowMemberResponse> response =
                    followService.getFollowers(memberId, loginMemberId, null, 2);

            // then
            assertThat(response.getHasNext()).isTrue();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getContent().get(0).getFollowId()).isEqualTo(10L);
            assertThat(response.getContent().get(1).getFollowId()).isEqualTo(9L);
            assertThat(response.getLastPostId()).isEqualTo(9L);
        }

        @Test
        @DisplayName("결과가 size 이하면 hasNext=false이다")
        void getFollowers_whenNoNext_returnsHasNextFalse() {
            Long memberId = 2L;
            Long loginMemberId = 1L;
            given(memberMapper.findById(memberId)).willReturn(Member.builder().id(memberId).build());
            given(followMapper.selectFollowers(memberId, loginMemberId, 10L, 3))
                    .willReturn(new ArrayList<>(List.of(followMember(9L, 3L))));

            SliceResponse<FollowMemberResponse> response =
                    followService.getFollowers(memberId, loginMemberId, 10L, 2);

            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getLastPostId()).isEqualTo(9L);
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 NoSuchElementException을 던진다")
        void getFollowers_whenMemberNotFound_throwsNoSuchElementException() {
            given(memberMapper.findById(99L)).willReturn(null);

            assertThatThrownBy(() -> followService.getFollowers(99L, 1L, null, 10))
                    .isInstanceOf(NoSuchElementException.class);

            verifyNoInteractions(followMapper);
        }
    }

    @Nested
    @DisplayName("팔로잉 목록")
    class GetFollowingsTest {

        @Test
        @DisplayName("팔로잉 목록을 커서 슬라이스로 반환한다")
        void getFollowings_returnsSlicedResult() {
            Long memberId = 2L;
            Long loginMemberId = 1L;
            given(memberMapper.findById(memberId)).willReturn(Member.builder().id(memberId).build());
            given(followMapper.selectFollowings(memberId, loginMemberId, null, 3))
                    .willReturn(new ArrayList<>(List.of(
                            followMember(7L, 8L),
                            followMember(6L, 9L)
                    )));

            SliceResponse<FollowMemberResponse> response =
                    followService.getFollowings(memberId, loginMemberId, null, 2);

            assertThat(response.getHasNext()).isFalse();
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getLastPostId()).isEqualTo(6L);
            verify(followMapper).selectFollowings(memberId, loginMemberId, null, 3);
        }

        @Test
        @DisplayName("존재하지 않는 회원이면 NoSuchElementException을 던진다")
        void getFollowings_whenMemberNotFound_throwsNoSuchElementException() {
            given(memberMapper.findById(99L)).willReturn(null);

            assertThatThrownBy(() -> followService.getFollowings(99L, 1L, null, 10))
                    .isInstanceOf(NoSuchElementException.class);

            verifyNoInteractions(followMapper);
        }
    }

    private static FollowMemberResponse followMember(Long followId, Long memberId) {
        return FollowMemberResponse.builder()
                .followId(followId)
                .memberId(memberId)
                .nickname("유저" + memberId)
                .profileImage("img.png")
                .isFollowing(false)
                .build();
    }
}
