package aibe.hosik.post.repository;

import aibe.hosik.apply.entity.PassStatus;
import aibe.hosik.post.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Post 조회 시 postSkills, skill 엔티티 즉시 로딩 지정
    @EntityGraph(attributePaths = {"postSkills", "postSkills.skill"})
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.postSkills ps LEFT JOIN FETCH ps.skill")
    List<Post> findAllWithSkills();

    @Query("""
            SELECT DISTINCT p 
            FROM Post p 
                LEFT JOIN FETCH p.postSkills ps
                LEFT JOIN FETCH p.user u
                JOIN p.applies a
            WHERE a.isSelected = :status
            OR a.user.id = :userId
            """)
    List<Post> findAllJoinedByUser(Long userId, PassStatus status);

    // PostDetail 조회 시 즉시 로딩 지정
    //@EntityGraph(attributePaths = {"postSkills", "postSkills.skill"})
    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.postSkills ps LEFT JOIN FETCH ps.skill WHERE p.id = :id")
    Optional<Post> findByIdWithSkills(@Param("id") Long id);

    List<Post> findByIsDoneFalseAndEndedAtLessThanEqual(LocalDate today);

    @Query("""
            SELECT DISTINCT p
            FROM Post p
                JOIN FETCH p.applies a
            WHERE ((p.user.id = :revieweeId AND a.user.id = :userId)
                OR (p.user.id = :userId AND a.user.id = :revieweeId))
            AND a.isSelected = :status
            """)
    List<Post> getAllPostsByTogether(Long revieweeId, Long userId, PassStatus status);
}
