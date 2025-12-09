package aibe.hosik.handler.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus statusCode;
    private final String message;

    public CustomException(final HttpStatus statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }

    public CustomException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.statusCode = errorCode.getStatusCode();
        this.message = errorCode.getMessage();
    }
}
