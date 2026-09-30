package net.likelion.bebc25.projectpatory.dto;

//complete 응답을 문자열 대신 JSON으로 반환
public record PaymentCompleteResponse(
        String paymentId,
        String status,          // PAID, FAILED, CANCELLED, READY(아직 결제 안 됨)
        Integer paidAmount,     // PAID일 때만 값이 있음
        String message          // 화면에 보여줄 안내 메시지
) {}
