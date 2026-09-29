package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.CommentCreateRequest;
import net.likelion.bebc25.projectpatory.dto.CommentResponse;
import net.likelion.bebc25.projectpatory.dto.CommentUpdateRequest;

public interface CommentService {

    // 댓글 작성
    CommentResponse createComment(Long postId, Long memberId, CommentCreateRequest request);

    // 댓글 수정
    CommentResponse updateComment(Long postId, Long commentId, Long memberId, CommentUpdateRequest request);

    // 댓글 삭제
    void deleteComment(Long postId, Long commentId, Long memberId);
}
