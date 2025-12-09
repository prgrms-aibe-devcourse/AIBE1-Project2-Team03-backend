package aibe.hosik.auth.controller;

import aibe.hosik.auth.JwtTokenProvider;
import aibe.hosik.auth.dto.LoginRequest;
import aibe.hosik.auth.dto.PasswordChangeRequest;
import aibe.hosik.auth.dto.SignUpRequest;
import aibe.hosik.auth.dto.SocialLoginRequest;
import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.user.entity.User;
import aibe.hosik.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증/인가 API")
@Slf4j
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authManager;
    private final JwtTokenProvider jwtProvider;

    /**
     * 회원가입
     * POST /auth/signup
     */
    @PostMapping("/signup")
    @Operation(summary = "ID/PW 회원가입")
    public ResponseEntity<?> signup(@Valid @RequestBody SignUpRequest req) {
        try {
            userService.register(req);
            return ResponseEntity.ok("회원가입 완료");
        } catch (CustomException e) {
            // CustomException은 GlobalExceptionHandler에서 처리됨
            throw e;
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INVALID_REQUEST_FORMAT);
        }
    }

    /**
     * 로그인
     * POST /auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "ID/PW 로그인")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
            String token = jwtProvider.generateToken(auth);

            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 인증 정보: {}", req.username());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        } catch (LockedException e) {
            log.warn("로그인 실패 - 계정 잠김: {}", req.username());
            throw new CustomException(ErrorCode.ACCOUNT_LOCKED);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("로그인 처리 중 예상치 못한 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }

    /**
     * 소셜 로그인
     * POST /auth/login/social
     */
    @PostMapping("/login/social")
    @Operation(summary = "소셜 로그인")
    public ResponseEntity<?> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        try {
            User user = userService.socialLogin(request);
            String token = jwtProvider.generateToken(user.getUsername());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (CustomException e) {
            // CustomException은 GlobalExceptionHandler에서 처리됨
            throw e;
        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_ERROR);
        }
    }

    /**
     * 비밀번호 변경
     * PATCH /auth/password
     */
    @PatchMapping("/password")
    @Operation(summary = "로컬 회원가입 회원용 비밀번호 변경")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequest req) {
        try {
            userService.changePassword(req);
            return ResponseEntity.noContent().build();
        } catch (CustomException e) {
            // CustomException은 GlobalExceptionHandler에서 처리됨
            throw e;
        } catch (Exception e) {
            log.error("비밀번호 변경 처리 중 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INVALID_REQUEST_FORMAT);
        }
    }
}