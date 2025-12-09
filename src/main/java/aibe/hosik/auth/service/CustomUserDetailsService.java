package aibe.hosik.auth.service;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.user.entity.User;
import aibe.hosik.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.trim().isEmpty()) {
            log.warn("유저명이 제공되지 않았습니다.");
            throw new CustomException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        try {
            return userRepository.findByUsername(username.trim())
                    .orElseThrow(() -> {
                        log.warn("사용자를 찾을 수 없습니다: {}", username);
                        return new CustomException(ErrorCode.NOT_FOUND_USER);
                    });
        } catch (CustomException e) {
            // 이미 CustomException인 경우 그대로 전달
            throw e;
        } catch (Exception e) {
            log.error("사용자 조회 중 데이터베이스 오류가 발생했습니다: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
        }
    }
}