package aibe.hosik.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 회원가입 요청을 담는 DTO
 */
public record SignUpRequest(
        @NotBlank(message = "아이디는 필수 입력값입니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        String password,

        @Email(message = "유효한 이메일 형식이어야 합니다.")
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        String email,


        @NotBlank(message = "이름은 필수 입력값입니다.")
        String name
) {
}
