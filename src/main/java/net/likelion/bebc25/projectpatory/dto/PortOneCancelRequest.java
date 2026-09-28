package net.likelion.bebc25.projectpatory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// JSON으로 바뀌면 { "reason": "취소 사유" } 모양이 된다
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PortOneCancelRequest {
    private String reason;  // 취소 사유
}
