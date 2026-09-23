package net.likelion.bebc25.projectpatory.service;

import net.likelion.bebc25.projectpatory.dto.PostListResponse;
import net.likelion.bebc25.projectpatory.dto.SliceResponse;
import net.likelion.bebc25.projectpatory.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    @Override
    public SliceResponse<PostListResponse> getPostListCursor(Long lastPostId, int size) {
        int limit = size + 1;
        List<PostListResponse> posts = postMapper.selectPostListCursor(lastPostId, limit);

        boolean hasNext = false;
        if (posts.size() > size) {
            hasNext = true;
            posts.remove(size);
        }

        Long nextCursorId = posts.isEmpty() ? null : posts.get(posts.size() - 1).getId();

        return new SliceResponse<>(posts, hasNext, nextCursorId);
    }
}