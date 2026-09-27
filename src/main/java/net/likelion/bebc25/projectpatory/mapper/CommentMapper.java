package net.likelion.bebc25.projectpatory.mapper;

import net.likelion.bebc25.projectpatory.dto.CommentCreateRequest;
import net.likelion.bebc25.projectpatory.dto.CommentResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    // 게시글의 댓글 전체 조회
    List<CommentResponse> findByPostId(@Param("postId") Long postId);

    // 각 댓글 전체 정보 조회
    CommentResponse findById(@Param("id") Long id);

    // 댓글 정보 저장
    void insert(CommentCreateRequest request);
}
