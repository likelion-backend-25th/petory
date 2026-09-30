package net.likelion.bebc25.projectpatory.dto;

// JSON으로 바뀌면 { "reason": "취소 사유" } 모양이 된다
public record PortOneCancelRequest(
        String reason   // 취소 사유
) {}
