package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.CommentCreateRequest;
import net.likelion.bebc25.projectpatory.dto.CommentResponse;
import net.likelion.bebc25.projectpatory.mapper.CommentMapper;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final PostMapper postMapper;

    @Transactional
    @Override
    public CommentResponse createComment(Long postId, Long memberId, CommentCreateRequest request) {
        // 게시글 존재 확인 및 예외처리
        if (postMapper.selectPostDetail(postId) == null) {
            throw new NoSuchElementException("해당 ID의 게시글을 찾을 수 없습니다. id=" + postId);
        }

        // CommentCreateResponse dto 값 설정
        request.setPostId(postId);
        request.setMemberId(memberId);
        commentMapper.insert(request);

        // 나머지 CommentCreateResponse dto 값 멤버와 조인하여 설정
        return commentMapper.findById(request.getId());
    }
}
