package aibe.hosik.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 비밀번호 변경 요청을 담는 DTO
 */
public record PasswordChangeRequest(
        @NotBlank
        @Email(message = "유효한 이메일 형식이어야 합니다")
        String email,

        @NotBlank
        String oldPassword,

        @NotBlank
        String newPassword
) {
}