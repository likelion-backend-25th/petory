package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.BookmarkToggleResponse;
import net.likelion.bebc25.projectpatory.dto.PostDetailResponse;
import net.likelion.bebc25.projectpatory.mapper.PostInteractionMapper;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PostBookmarkServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostInteractionMapper postInteractionMapper;

    @InjectMocks
    private PostBookmarkServiceImpl postBookmarkService;

    @Nested
    @DisplayName("북마크 토글")
    class ToggleBookmarkTest {

        @Test
        @DisplayName("미북마크 상태에서 토글하면 insert 후 bookmarked=true를 반환한다")
        void toggleBookmark_whenNotBookmarked_insertsAndReturnsTrue() {
            Long postId = 3L;
            Long memberId = 1L;
            given(postMapper.selectPostDetail(postId)).willReturn(PostDetailResponse.builder().id(postId).build());
            given(postInteractionMapper.existsBookmark(postId, memberId)).willReturn(false);

            BookmarkToggleResponse response = postBookmarkService.toggleBookmark(postId, memberId);

            assertThat(response.getPostId()).isEqualTo(postId);
            assertThat(response.isBookmarked()).isTrue();
            verify(postInteractionMapper).insertBookmark(postId, memberId);
            verify(postInteractionMapper, never()).deleteBookmark(postId, memberId);
        }

        @Test
        @DisplayName("북마크 상태에서 토글하면 delete 후 bookmarked=false를 반환한다")
        void toggleBookmark_whenBookmarked_deletesAndReturnsFalse() {
            Long postId = 3L;
            Long memberId = 1L;
            given(postMapper.selectPostDetail(postId)).willReturn(PostDetailResponse.builder().id(postId).build());
            given(postInteractionMapper.existsBookmark(postId, memberId)).willReturn(true);

            BookmarkToggleResponse response = postBookmarkService.toggleBookmark(postId, memberId);

            assertThat(response.getPostId()).isEqualTo(postId);
            assertThat(response.isBookmarked()).isFalse();
            verify(postInteractionMapper).deleteBookmark(postId, memberId);
            verify(postInteractionMapper, never()).insertBookmark(postId, memberId);
        }

        @Test
        @DisplayName("존재하지 않는 게시글이면 NoSuchElementException을 던진다")
        void toggleBookmark_whenPostMissing_throwsException() {
            given(postMapper.selectPostDetail(99L)).willReturn(null);

            assertThatThrownBy(() -> postBookmarkService.toggleBookmark(99L, 1L))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("찾을 수 없습니다");

            verifyNoInteractions(postInteractionMapper);
        }
    }
}
