package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.*;

public interface PostLikeService {
    void toggleLike(Long postId, Long memberId);
}