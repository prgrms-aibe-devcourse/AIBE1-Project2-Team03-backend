package aibe.hosik.skill.repository;


import aibe.hosik.post.entity.Post;
import aibe.hosik.skill.entity.PostSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostSkillRepository extends JpaRepository<PostSkill, Long> {
    // Post 엔티티로 Post 게시글과 연관된 모든 PostSkill 목록 조회
    List<PostSkill> findByPost(Post post);

    // Post ID로 해당 글과 연관된 모든 스킬 이름 직접 조회
    @Query("SELECT s.skill.name FROM PostSkill s WHERE s.post.id = :postId")
    List<String> findSkillByPostId(@Param("postId") Long postId);

    void deleteByPostId(Long postId);

    Long post(Post post);
}
