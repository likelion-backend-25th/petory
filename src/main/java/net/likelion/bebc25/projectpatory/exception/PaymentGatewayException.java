package net.likelion.bebc25.projectpatory.exception;

// 변경: 신규 추가 - PortOne API 호출 실패(인증 오류, 5xx, 네트워크 오류, 응답 파싱 실패)를 502로 응답하기 위한 예외
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message) {
        super(message);
    }

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
