package net.likelion.bebc25.projectpatory.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostInteractionMapper {

    // 좋아요 여부 확인
    boolean existsLike(@Param("postId") Long postId, @Param("memberId") Long memberId);

    // 좋아요 등록
    void insertLike(@Param("postId") Long postId, @Param("memberId") Long memberId);

    // 좋아요 취소
    int deleteLike(@Param("postId") Long postId, @Param("memberId") Long memberId);

    // 좋아요 집계
    long countLikes(@Param("postId") Long postId);

    // 고유 조회수 처리(글 최초열람시에만 조회수 증가)
    void insertViewIgnore(@Param("postId") Long postId, @Param("memberId") Long memberId);

    // 조회수 집계
    long countViews(@Param("postId") Long postId);
}
