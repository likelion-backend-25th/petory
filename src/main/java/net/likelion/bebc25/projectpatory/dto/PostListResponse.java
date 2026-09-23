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


    private Long id;                  // post_main.id
    private Integer type;             // post_main.type (1: 일반, 2: Q&A)
    private String content;           // post_main.content
    private String bgmUrl;            // post_main.bgm_url
    private Boolean isSubscriberOnly; // post_main.is_subscriber_only (0: false, 1: true)
    private String hashtags;          // post_main.hashtags
    private LocalDateTime createdAt;  // post_main.created_at
    private LocalDateTime updatedAt;  // post_main.updated_at

}