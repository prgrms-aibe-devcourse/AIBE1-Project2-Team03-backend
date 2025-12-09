package aibe.hosik.user.service;

import aibe.hosik.auth.dto.PasswordChangeRequest;
import aibe.hosik.auth.dto.SignUpRequest;
import aibe.hosik.auth.dto.SocialLoginRequest;
import aibe.hosik.profile.entity.Profile;
import aibe.hosik.profile.repository.ProfileRepository;
import aibe.hosik.user.entity.Role;
import aibe.hosik.user.entity.User;
import aibe.hosik.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 새로운 사용자를 등록합니다. (회원가입)
     *
     * @param req 회원가입 요청 DTO
     * @throws IllegalArgumentException 아이디 또는 이메일이 이미 사용 중일 경우 발생
     */
    @Transactional // 트랜잭션 관리
    public void register(SignUpRequest req) {
        // 이미 사용 중인 아이디인지 확인
        userRepository.findByUsername(req.username())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
                });
        // 이미 사용 중인 이메일인지 확인
        userRepository.findByEmail(req.email())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
                });

        // User 객체 생성 및 필드 설정
        User user = User.builder()
                .username(req.username())
                .password(passwordEncoder.encode(req.password()))
                .email(req.email())
                .name(req.name())
                .roles(Role.USER)
                .build();

        Profile profile = Profile.builder()
                .user(user)
                .nickname(req.name())
                .build();

        user.linkProfile(profile);

        userRepository.save(user);
    }

    @Transactional
    public User socialLogin(SocialLoginRequest request) {
        return userRepository.findBySocialTypeAndSocialId(request.type(), request.socialId())
                .orElseGet(() -> {
                    User user = User.builder()
                            .name(request.name())
                            .username(UUID.randomUUID().toString())
                            .socialId(request.socialId())
                            .socialType(request.type())
                            .build();

                    Profile profile = Profile.builder()
                            .user(user)
                            .nickname(request.name())
                            .build();

                    user.linkProfile(profile);

                    return userRepository.save(user);
                });
    }

    /**
     * 사용자의 비밀번호를 변경합니다.
     *
     * @param req 비밀번호 변경 요청 DTO
     * @throws UsernameNotFoundException 해당 이메일의 사용자를 찾을 수 없을 경우 발생
     * @throws IllegalArgumentException  현재 비밀번호가 일치하지 않을 경우 발생
     */
    @Transactional // 트랜잭션 관리
    public void changePassword(PasswordChangeRequest req) {
        // email로 사용자 찾기
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + req.email())
                );

        // 기존 비밀번호 검증
        if (!passwordEncoder.matches(req.oldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 인코딩 후 저장
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }
}
