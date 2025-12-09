package aibe.hosik.skill.repository;

import aibe.hosik.skill.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String skillName);

    @Query("""
            SELECT DISTINCT s.name
            FROM Skill s
                LEFT JOIN FETCH PostSkill ps ON s.id = ps.skill.id
            WHERE ps.post.id = :postId
            """)
    List<String> findByPostId(Long postId);
}
