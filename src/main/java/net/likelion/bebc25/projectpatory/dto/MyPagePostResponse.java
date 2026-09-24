package net.likelion.bebc25.projectpatory.dto;

public record MyPagePostResponse(
        Long id,
        String content,
        boolean isSubscriberOnly,
        String hashtags,
        String imageUrl,
        int likeCount
) {}
