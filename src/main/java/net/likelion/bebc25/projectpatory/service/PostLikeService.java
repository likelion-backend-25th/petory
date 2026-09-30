package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.*;

public interface PostLikeService {
    public LikeToggleResponse toggleLike(Long postId, Long memberId);
}