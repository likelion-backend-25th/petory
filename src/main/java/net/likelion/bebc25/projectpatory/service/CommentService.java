package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.CommentCreateRequest;
import net.likelion.bebc25.projectpatory.dto.CommentResponse;

public interface CommentService {

    CommentResponse createComment(Long postId, Long memberId, CommentCreateRequest request);
}
