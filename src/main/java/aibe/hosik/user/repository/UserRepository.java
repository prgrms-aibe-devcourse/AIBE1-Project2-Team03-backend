package aibe.hosik.user.repository;

import aibe.hosik.user.entity.SocialType;
import aibe.hosik.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // username으로 사용자 찾기 (로컬 로그인 시 주로 사용)
    Optional<User> findByUsername(String username);

    // email로 사용자 찾기
    Optional<User> findByEmail(String email);

    // 소셜 타입과 소셜 ID로 사용자 찾기 (소셜 로그인 시 주로 사용)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);
}
