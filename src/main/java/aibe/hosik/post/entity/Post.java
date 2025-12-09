package aibe.hosik.post.entity;

import aibe.hosik.apply.entity.Apply;
import aibe.hosik.common.TimeEntity;
import aibe.hosik.post.dto.PostUpdateRequest;
import aibe.hosik.skill.entity.PostSkill;
import aibe.hosik.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Post extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    @Builder.Default
    private boolean isDone = false;

    @Column
    private int headCount;

    @Column
    private String image;

    @Column
    private String requirementPersonality;

    @Column(nullable = false)
    private LocalDate endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // 양방향 매핑
    @OneToMany(mappedBy = "post")
    @BatchSize(size = 20)
    @Builder.Default
    private List<PostSkill> postSkills = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    @BatchSize(size = 20)
    @Builder.Default
    private List<Apply> applies = new ArrayList<>();

    // 게시글 수정 메서드
    public void updatePatch(PostUpdateRequest dto) {
        if (dto.title() != null) this.title = dto.title();
        if (dto.content() != null) this.content = dto.content();
        if (dto.requirementPersonality() != null) this.requirementPersonality = dto.requirementPersonality();
        if (dto.headCount() != null) this.headCount = dto.headCount();
        if (dto.endedAt() != null) this.endedAt = dto.endedAt();
        if (dto.category() != null) this.category = dto.category();
        if (dto.type() != null) this.type = dto.type();
    }
}
