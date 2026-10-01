package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.BookmarkToggleResponse;

public interface PostBookmarkService {
    BookmarkToggleResponse toggleBookmark(Long postId, Long memberId);
}
