package aibe.hosik.analysis.repository;

import aibe.hosik.analysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findByApplyId(Long applyId);

    @Query("SELECT a FROM Analysis a WHERE a.apply.id = :applyId ")
    Optional<Analysis> findLatestByApplyId(@Param("applyId") Long applyId);
}
