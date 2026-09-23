package net.likelion.bebc25.projectpatory.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class SliceResponse<T> {
    private final List<T> content;     // 실제 게시글 데이터 리스트
    private final Boolean hasNext;     // 다음 스크롤할 페이지가 있는지 (true/false)
    private final Long lastPostId;     // 이번에 전달한 게시글 중 마지막 게시글 ID (다음 커서용)

    public SliceResponse(List<T> content, boolean hasNext, Long lastPostId) {
        this.content = content;
        this.hasNext = hasNext;
        this.lastPostId = lastPostId;
    }
}