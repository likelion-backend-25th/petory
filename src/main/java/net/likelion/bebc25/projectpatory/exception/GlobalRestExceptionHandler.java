package net.likelion.bebc25.projectpatory.exception;

import lombok.extern.slf4j.Slf4j;
import net.likelion.bebc25.projectpatory.dto.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice
public class GlobalRestExceptionHandler {

    // @Valid 유효성 검증 실패할 경우에 호출됨(400 Bad Request 응답)
    // 클라이언트가 전송한 DTO의 제약조건(@NotNull, @NotBlank, @Size 등)을 위반할 경우 스프링이 발생시키는 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        BindingResult bindingResult = ex.getBindingResult();
        // 스프링이 만든 FieldError를 ApiErrorResponse의 FieldErrorDetail로 변환하기 위해서 for문 대신 Stream API를 사용함
        List<ApiErrorResponse.FieldErrorDetail> fieldErrors = bindingResult.getFieldErrors().stream()
                .map(error -> new ApiErrorResponse.FieldErrorDetail(
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                ))
                .toList();

        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, fieldErrors);
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getHttpStatus()).body(response);
    }

    // 비즈니스 업무 규칙 위반시 호출됨(400 Bad Request 응답)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage());
        return ResponseEntity.status(ErrorCode.BUSINESS_RULE_VIOLATION.getHttpStatus()).body(response);
    }

    // 요청한 자원이 없을 때(404 Not Found 응답)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNoSuchElementException(NoSuchElementException ex) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus()).body(response);
    }

    // 권한이 부족 할때(403 Forbidden 응답)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.FORBIDDEN_OPERATION/*, ex.getMessage()*/);
        return ResponseEntity.status(ErrorCode.FORBIDDEN_OPERATION.getHttpStatus()).body(response);
    }

    // 서버 내부 오류가 발생했을 때(500 Internal Server Error 응답)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {
        log.error("exction발생", ex);
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
    }
    // 인증 실패 또는 유효하지 않은 자격 증명 예외 처리 (401 Unauthorized 응답)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ApiErrorResponse response = ApiErrorResponse.of(ErrorCode.UNAUTHORIZED_ACCESS, ex.getMessage());
        return ResponseEntity.status(ErrorCode.UNAUTHORIZED_ACCESS.getHttpStatus()).body(response);
    }
}