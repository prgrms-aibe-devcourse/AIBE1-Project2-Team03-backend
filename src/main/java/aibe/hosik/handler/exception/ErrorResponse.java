package aibe.hosik.handler.exception;

import java.time.LocalDateTime;
import java.util.UUID;

public record ErrorResponse(
        LocalDateTime timeStamp,
        UUID errorId,
        String message
) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(LocalDateTime.now(), UUID.randomUUID(), message);
    }
}
