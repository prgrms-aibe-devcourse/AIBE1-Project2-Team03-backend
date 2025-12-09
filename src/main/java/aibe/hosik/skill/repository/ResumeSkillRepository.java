package aibe.hosik.skill.repository;


import aibe.hosik.skill.entity.ResumeSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResumeSkillRepository extends JpaRepository<ResumeSkill, Long> {
    /**
     * 특정 이력서에 연결된 모든 스킬, 스킬 정보 조회
     */
    @Query("SELECT rs FROM ResumeSkill rs JOIN FETCH rs.skill WHERE rs.resume.id = :resumeId")
    List<ResumeSkill> findByResumeId(@Param("resumeId") Long resumeId);
}