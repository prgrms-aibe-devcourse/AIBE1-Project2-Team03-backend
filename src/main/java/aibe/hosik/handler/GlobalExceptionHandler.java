package aibe.hosik.handler;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.handler.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;

/**
 * 애플리케이션 전역에서 발생하는 예외를 처리하는 핸들러입니다.
 * 다양한 유형의 예외에 대해 일관된 응답 형식을 제공합니다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 처리되지 않은 모든 예외를 처리하는 메서드입니다.
     *
     * @param exception 발생한 예외
     * @param request   클라이언트 요청 정보
     * @return 내부 서버 오류(500) 상태 코드와 에러 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(final Exception exception, final HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(exception.getMessage());

        log.error(
                "Error ID: {}, Request URL: {}, Message: {}",
                response.errorId(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * 애플리케이션에서 정의한 사용자 정의 예외를 처리하는 메서드입니다.
     *
     * @param exception 사용자 정의 예외
     * @param request   클라이언트 요청 정보
     * @return 예외에 정의된 상태 코드와 에러 응답
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleCustomException(final CustomException exception, final HttpServletRequest request) {
        ErrorResponse response = ErrorResponse.of(exception.getMessage());

        log.error(
                "Error ID: {}, Request URL: {}, Message: {}",
                response.errorId(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(exception.getStatusCode())
                .body(response);
    }

    /**
     * Spring의 @Valid 검증 실패 시 발생하는 예외를 처리하는 메서드입니다.
     * 주로 @RequestBody, @ModelAttribute 등에 적용된 검증 실패 시 발생합니다.
     *
     * @param exception @Valid 검증 실패 예외
     * @param request   클라이언트 요청 정보
     * @return 잘못된 요청(400) 상태 코드와 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(exception.getMessage());

        log.error(
                "Error ID: {}, Request URL: {}, Message: {}",
                response.errorId(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 주로 컨트롤러 메서드 파라미터의 유효성 검증 실패 시 발생합니다.
     *
     * @param exception 핸들러 메서드 유효성 검증 예외
     * @param request   클라이언트 요청 정보
     * @return 잘못된 요청(400) 상태 코드와 유효성 검증 오류 정보를 포함한 응답
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    protected ResponseEntity<?> handleHandlerMethodValidationException(
            final HandlerMethodValidationException exception,
            final HttpServletRequest request
    ) {
        final List<?> validationResults = exception.getAllErrors();

        ErrorResponse response = ErrorResponse.of(exception.getMessage());

        log.error(
                "Error ID: {}, Request URL: {}, Message: {}",
                response.errorId(),
                request.getRequestURI(),
                validationResults
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // ==================== 인증 관련 예외 처리 ====================

    /**
     * 잘못된 로그인 자격 증명 시 발생하는 예외를 처리합니다.
     *
     * @param exception 잘못된 자격 증명 예외
     * @param request   클라이언트 요청 정보
     * @return 인증 실패(401) 상태 코드와 에러 응답
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(
            final BadCredentialsException exception,
            final HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_CREDENTIALS.getMessage());

        log.warn(
                "BadCredentials - Error ID: {}, Request URL: {}, IP: {}",
                response.errorId(),
                request.getRequestURI(),
                getClientIpAddress(request)
        );

        return ResponseEntity
                .status(ErrorCode.INVALID_CREDENTIALS.getStatusCode())
                .body(response);
    }

    /**
     * 계정이 잠긴 경우 발생하는 예외를 처리합니다.
     *
     * @param exception 계정 잠금 예외
     * @param request   클라이언트 요청 정보
     * @return 인증 실패(401) 상태 코드와 에러 응답
     */
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<?> handleLockedException(
            final LockedException exception,
            final HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.ACCOUNT_LOCKED.getMessage());

        log.warn(
                "AccountLocked - Error ID: {}, Request URL: {}, IP: {}",
                response.errorId(),
                request.getRequestURI(),
                getClientIpAddress(request)
        );

        return ResponseEntity
                .status(ErrorCode.ACCOUNT_LOCKED.getStatusCode())
                .body(response);
    }

    /**
     * 사용자를 찾을 수 없는 경우 발생하는 예외를 처리합니다.
     *
     * @param exception 사용자 없음 예외
     * @param request   클라이언트 요청 정보
     * @return 찾을 수 없음(404) 상태 코드와 에러 응답
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(
            final UsernameNotFoundException exception,
            final HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(ErrorCode.USER_NOT_FOUND_BY_USERNAME.getMessage());

        log.warn(
                "UsernameNotFound - Error ID: {}, Request URL: {}, Message: {}",
                response.errorId(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(ErrorCode.USER_NOT_FOUND_BY_USERNAME.getStatusCode())
                .body(response);
    }

    /**
     * OAuth2 인증 실패 시 발생하는 예외를 처리합니다.
     *
     * @param exception OAuth2 인증 예외
     * @param request   클라이언트 요청 정보
     * @return 잘못된 요청(400) 상태 코드와 에러 응답
     */
    @ExceptionHandler(OAuth2AuthenticationException.class)
    public ResponseEntity<?> handleOAuth2AuthenticationException(
            final OAuth2AuthenticationException exception,
            final HttpServletRequest request
    ) {
        ErrorResponse response = ErrorResponse.of(exception.getMessage());

        log.error(
                "OAuth2Authentication - Error ID: {}, Request URL: {}, Message: {}",
                response.errorId(),
                request.getRequestURI(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 클라이언트의 실제 IP 주소를 가져오는 헬퍼 메서드입니다.
     * 프록시나 로드밸런서를 통해 들어오는 요청의 실제 IP를 추출합니다.
     *
     * @param request HTTP 요청 객체
     * @return 클라이언트의 IP 주소
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}