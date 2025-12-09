package aibe.hosik.auth.dto;

import aibe.hosik.user.entity.SocialType;
import jakarta.validation.constraints.NotBlank;

/**
 * 소셜 로그인 요청을 담는 DTO
 */
public record SocialLoginRequest(
        @NotBlank(message = "아이디는 필수 입력값입니다.")
        String name,

        @NotBlank(message = "socialId는 필수 입력값입니다.")
        String socialId,

        SocialType type
) {
}