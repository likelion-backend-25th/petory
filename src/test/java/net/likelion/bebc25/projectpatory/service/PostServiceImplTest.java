package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.MyPagePostResponse;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    @DisplayName("마이페이지 메인 게시글 목록을 조회한다")
    void getMyMainPosts_returnsMappedPosts() {
        // given
        Long memberId = 1L;
        List<MyPagePostResponse> posts = List.of(
                new MyPagePostResponse(10L, "산책했어요", false, "#산책", "img1.png", 3),
                new MyPagePostResponse(11L, "간식 추천", true, "#간식", "img2.png", 7)
        );
        given(postMapper.getMyMainPosts(memberId)).willReturn(posts);

        // when
        List<MyPagePostResponse> result = postService.getMyMainPosts(memberId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).content()).isEqualTo("산책했어요");
        assertThat(result.get(1).isSubscriberOnly()).isTrue();
        verify(postMapper).getMyMainPosts(memberId);
    }

    @Test
    @DisplayName("마이페이지 QnA 게시글 목록을 조회한다")
    void getMyQnAPosts_returnsMappedPosts() {
        // given
        Long memberId = 1L;
        List<MyPagePostResponse> posts = List.of(
                new MyPagePostResponse(20L, "사료 추천 부탁", false, "#QnA", null, 1)
        );
        given(postMapper.getMyQnAPosts(memberId)).willReturn(posts);

        // when
        List<MyPagePostResponse> result = postService.getMyQnAPosts(memberId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(20L);
        assertThat(result.get(0).content()).isEqualTo("사료 추천 부탁");
        verify(postMapper).getMyQnAPosts(memberId);
    }

    @Test
    @DisplayName("본인 북마크 목록을 조회한다")
    void getMyBookmarks_whenOwnBookmarks_returnsMappedPosts() {
        // given
        Long memberId = 1L;
        List<MyPagePostResponse> bookmarks = List.of(
                new MyPagePostResponse(30L, "북마크한 글", false, "#북마크", "book.png", 5)
        );
        given(postMapper.getMyBookmarks(memberId)).willReturn(bookmarks);

        // when
        List<MyPagePostResponse> result = postService.getMyBookmarks(memberId, memberId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(30L);
        verify(postMapper).getMyBookmarks(memberId);
    }

    @Test
    @DisplayName("타인 북마크 조회 시 IllegalStateException을 던진다")
    void getMyBookmarks_whenOtherMember_throwsException() {
        // given
        Long targetMemberId = 2L;
        Long loginMemberId = 1L;

        // when & then
        assertThatThrownBy(() -> postService.getMyBookmarks(targetMemberId, loginMemberId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("본인의 북마크만 조회할 수 있습니다.");

        verify(postMapper, never()).getMyBookmarks(targetMemberId);
    }
}
