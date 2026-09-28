package net.likelion.bebc25.projectpatory.service;

import lombok.RequiredArgsConstructor;
import net.likelion.bebc25.projectpatory.dto.LikeToggleResponse;
import net.likelion.bebc25.projectpatory.mapper.PostInteractionMapper;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class PostLikeServiceImpl implements PostLikeService {

    private final PostMapper postMapper;
    private final PostInteractionMapper postInteractionMapper;

    @Override
    public LikeToggleResponse toggleLike(Long postId, Long memberId) {
        // 게시글 존재 확인 및 예외처리
        if (postMapper.selectPostDetail(postId) == null) {
            throw new NoSuchElementException("해당 ID의 게시글을 찾을 수 없습니다. id=" + postId);
        }

        boolean liked;
        // 좋아요 활성화 상태에서 취소 이벤트 발생
        if (postInteractionMapper.existsLike(postId, memberId)) {
            postInteractionMapper.deleteLike(postId, memberId);
            liked = false;

        }
        // 좋아요 비활성화 상태에서 등록 이벤트 발생
        else {
            postInteractionMapper.insertLike(postId, memberId);
            liked = true;
        }

        return LikeToggleResponse.builder()
                .liked(liked)
                .likeCount(postInteractionMapper.countLikes(postId))
                .build();
    }
}
