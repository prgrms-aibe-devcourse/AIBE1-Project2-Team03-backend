package aibe.hosik.resume.repository;

import aibe.hosik.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    @Modifying
    @Query("""
            UPDATE Resume r
            SET r.isMain = false
            WHERE r.isMain = true
            AND r.user.id = :userId
            """)
    void resetMainResumeFlag(Long userId);

    Optional<Resume> findByIdAndUserId(Long id, Long userId);

    List<Resume> findAllByUserId(Long userId);

    @Query("""
            SELECT r
            FROM Resume r
            WHERE r.isMain = true
            """)
    List<Resume> findAllMainResumes();

    void deleteByIdAndUserId(Long id, Long userId);
}