package aibe.hosik.handler.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    NOT_FOUND_POST(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    NOT_FOUND_RESUME(HttpStatus.NOT_FOUND, "자기소개서를 찾을 수 없습니다."),
    NOT_FOUND_PROFILE(HttpStatus.NOT_FOUND, "프로필을 찾을 수 없습니다."),
    NOT_FOUND_REVIEW(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."),
    NOT_FOUND_APPLY(HttpStatus.NOT_FOUND, "지원서를 찾을 수 없습니다."),

    LOGIN_REQUIRED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),

    POST_AUTHOR_FORBIDDEN(HttpStatus.FORBIDDEN, "모집글 작성자만 접근할 수 있습니다."),
    POST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "모집글 작성자만 수정, 삭제할 수 있습니다."),

    RESUME_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 이력서만 사용할 수 있습니다."),
    APPLY_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "본인의 지원서만 삭제할 수 있습니다."),

    INVALID_DATA_FORMAT(HttpStatus.BAD_REQUEST, "날짜 형식이 잘못되었습니다. 형식: YYYY-MM-DD (예: 2025-12-31)"),

    // 인증/인가 관련 에러코드들 추가
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다."),
    JWT_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT 토큰이 제공되지 않았습니다."),

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "잘못된 아이디 또는 비밀번호입니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "계정이 잠겼습니다. 관리자에게 문의하세요."),
    USER_NOT_FOUND_BY_USERNAME(HttpStatus.NOT_FOUND, "해당 아이디로 사용자를 찾을 수 없습니다."),

    SOCIAL_LOGIN_ERROR(HttpStatus.BAD_REQUEST, "소셜 로그인 처리 중 오류가 발생했습니다."),
    UNSUPPORTED_SOCIAL_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 타입입니다."),
    INVALID_SOCIAL_USER_INFO(HttpStatus.BAD_REQUEST, "소셜 로그인 사용자 정보가 유효하지 않습니다."),

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    INVALID_REQUEST_FORMAT(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "필수 입력값이 누락되었습니다.");

    private final HttpStatus statusCode;
    private final String message;
}
