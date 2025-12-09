package aibe.hosik.auth.service;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.user.entity.Role;
import aibe.hosik.user.entity.SocialType;
import aibe.hosik.user.entity.User;
import aibe.hosik.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;

    @SuppressWarnings("unchecked")
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            String userNameAttributeName = userRequest.getClientRegistration()
                    .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

            Map<String, Object> attributes = oAuth2User.getAttributes();

            // 소셜 로그인 타입별 사용자 정보 추출
            SocialUserInfo socialUserInfo = extractSocialUserInfo(registrationId, attributes, userNameAttributeName);

            // 사용자 조회 또는 생성
            User user = findOrCreateUser(socialUserInfo);

            return new DefaultOAuth2User(
                    Collections.singleton(new SimpleGrantedAuthority(user.getRoles().name())),
                    attributes,
                    userNameAttributeName
            );

        } catch (CustomException e) {
            log.error("소셜 로그인 처리 중 오류 발생: {}", e.getMessage());
            throw new OAuth2AuthenticationException(e.getMessage());
        } catch (Exception e) {
            log.error("소셜 로그인 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException("소셜 로그인 처리 중 오류가 발생했습니다.");
        }
    }

    private SocialUserInfo extractSocialUserInfo(String registrationId, Map<String, Object> attributes, String userNameAttributeName) {
        return switch (registrationId.toLowerCase()) {
            case "github" -> extractGithubUserInfo(attributes, userNameAttributeName);
            case "kakao" -> extractKakaoUserInfo(attributes, userNameAttributeName);
            case "google" -> extractGoogleUserInfo(attributes, userNameAttributeName);
            default -> {
                log.warn("지원하지 않는 소셜 로그인 타입: {}", registrationId);
                throw new CustomException(ErrorCode.UNSUPPORTED_SOCIAL_TYPE);
            }
        };
    }

    private SocialUserInfo extractGithubUserInfo(Map<String, Object> attributes, String userNameAttributeName) {
        if (!attributes.containsKey("login") || !attributes.containsKey(userNameAttributeName)) {
            log.warn("GitHub 사용자 정보가 유효하지 않습니다: {}", attributes);
            throw new CustomException(ErrorCode.INVALID_SOCIAL_USER_INFO);
        }

        String socialId = attributes.get(userNameAttributeName).toString();
        String username = "github_" + attributes.get("login");
        String name = (String) attributes.get("name");

        return new SocialUserInfo(username, name, socialId, SocialType.GITHUB);
    }

    private SocialUserInfo extractKakaoUserInfo(Map<String, Object> attributes, String userNameAttributeName) {
        if (!attributes.containsKey(userNameAttributeName)) {
            log.warn("Kakao 사용자 정보가 유효하지 않습니다: {}", attributes);
            throw new CustomException(ErrorCode.INVALID_SOCIAL_USER_INFO);
        }

        String socialId = attributes.get(userNameAttributeName).toString();
        String username = "kakao_" + socialId;

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            throw new CustomException(ErrorCode.INVALID_SOCIAL_USER_INFO);
        }

        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");
        if (kakaoProfile == null || !kakaoProfile.containsKey("nickname")) {
            log.warn("Kakao 프로필 정보가 유효하지 않습니다: {}", kakaoProfile);
            throw new CustomException(ErrorCode.INVALID_SOCIAL_USER_INFO);
        }

        String name = (String) kakaoProfile.get("nickname");

        return new SocialUserInfo(username, name, socialId, SocialType.KAKAO);
    }

    private SocialUserInfo extractGoogleUserInfo(Map<String, Object> attributes, String userNameAttributeName) {
        if (!attributes.containsKey(userNameAttributeName)) {
            log.warn("Google 사용자 정보가 유효하지 않습니다: {}", attributes);
            throw new CustomException(ErrorCode.INVALID_SOCIAL_USER_INFO);
        }

        String socialId = attributes.get(userNameAttributeName).toString();
        String username = "google_" + socialId;
        String name = (String) attributes.get("name");

        return new SocialUserInfo(username, name, socialId, SocialType.GOOGLE);
    }

    private User findOrCreateUser(SocialUserInfo socialUserInfo) {
        try {
            Optional<User> existingUser = userRepository.findBySocialTypeAndSocialId(
                    socialUserInfo.socialType(), socialUserInfo.socialId()
            );

            return existingUser.orElseGet(() -> {
                log.info("OAuth2 신규 회원가입: {}", socialUserInfo.username());
                User newUser = User.builder()
                        .username(socialUserInfo.username())
                        .password(null)
                        .email(null)
                        .name(socialUserInfo.name())
                        .roles(Role.USER)
                        .socialType(socialUserInfo.socialType())
                        .socialId(socialUserInfo.socialId())
                        .build();
                return userRepository.save(newUser);
            });
        } catch (Exception e) {
            log.error("사용자 조회/생성 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.SOCIAL_LOGIN_ERROR);
        }
    }

    // 소셜 사용자 정보를 담는 record
    private record SocialUserInfo(
            String username,
            String name,
            String socialId,
            SocialType socialType
    ) {
    }
}