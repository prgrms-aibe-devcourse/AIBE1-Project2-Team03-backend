package aibe.hosik.auth;

import aibe.hosik.auth.service.CustomUserDetailsService;
import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        try {
            String authHeader = req.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // JWT 토큰 검증 (예외 발생 시 CustomException으로 처리됨)
                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsername(token);

                    try {
                        // DB에서 사용자 조회
                        User user = customUserDetailsService.loadUserByUsername(username);

                        Authentication auth = new UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities()
                        );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } catch (UsernameNotFoundException e) {
                        log.warn("토큰은 유효하지만 사용자를 찾을 수 없습니다: {}", username);
                        throw new CustomException(ErrorCode.NOT_FOUND_USER);
                    }
                }
            }

            chain.doFilter(req, res);

        } catch (CustomException e) {
            // CustomException을 JSON 응답으로 변환하여 클라이언트에 전달
            handleCustomException(res, e);
        } catch (Exception e) {
            // 예상치 못한 예외가 발생한 경우
            log.error("JWT 필터에서 예상치 못한 오류가 발생했습니다: {}", e.getMessage(), e);
            handleCustomException(res, new CustomException(ErrorCode.AUTHENTICATION_FAILED));
        }
    }

    private void handleCustomException(HttpServletResponse response, CustomException exception)
            throws IOException {
        response.setStatus(exception.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // ErrorResponse와 동일한 형식으로 응답 생성
        Map<String, Object> errorResponse = Map.of(
                "timeStamp", java.time.LocalDateTime.now().toString(),
                "errorId", java.util.UUID.randomUUID().toString(),
                "message", exception.getMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}