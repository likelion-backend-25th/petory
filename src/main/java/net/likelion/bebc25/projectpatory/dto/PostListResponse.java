package net.likelion.bebc25.projectpatory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {

    // 1. post_main (피드 기본 정보)
    private Long id;                  // 피드 게시글 고유 ID (post_main.id)
    private Integer type;             // 게시글 유형 (1: 일반 피드, 2: Q&A)
    private String content;           // 피드 본문 내용
    private String bgmUrl;            // 배경음악 S3 URL
    private Boolean isSubscriberOnly; // 유료 구독자 전용 여부 (false: 전체공개, true: 구독자전용)
    private String hashtags;          // 해시태그 문자열 (예: "#강아지 #산책")
    private LocalDateTime createdAt;  // 최초 작성 일시
    private LocalDateTime updatedAt;  // 최종 수정 일시

    // 2. member (작성자 프로필 정보)
    private Long memberId;            // 작성자 회원 ID
    private String nickname;          // 작성자 닉네임
    private String profileImage;      // 작성자 프로필 이미지 S3 URL

    // 3. post_image (첨부 이미지 목록)
    private List<String> imageUrls;   // 첨부 이미지 S3 URL 리스트 (sort_order 순 정렬)

    // 4. 반응/소셜 수치 (집계)
    private Long likeCount;           // 좋아요 수
    private Long commentCount;        // 댓글 수
}