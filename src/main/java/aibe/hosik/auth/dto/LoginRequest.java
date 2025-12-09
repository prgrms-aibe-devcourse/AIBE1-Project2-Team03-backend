package aibe.hosik.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청을 담는 DTO
 */
public record LoginRequest(
        @NotBlank(message = "아이디는 필수 입력값입니다.")
        @Schema(description = "ID", example = "eeee")
        String username,

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        @Schema(description = "PW", example = "eeeeeeee")
        String password
) {
}