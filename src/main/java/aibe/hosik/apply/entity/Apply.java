package aibe.hosik.apply.entity;

import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.common.TimeEntity;
import aibe.hosik.post.entity.Post;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Apply extends TimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private PassStatus isSelected = PassStatus.PENDING;

    @Column
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // ai 파트 연결
    @OneToMany(mappedBy = "apply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Analysis> analysis = new ArrayList<>();

    /**
     * 지정된 모집글, 사용자, 이력서 및 이유를 바탕으로 Apply 객체를 생성한다.
     * 생성된 객체는 기본적으로 미선정 상태로 설정된다.
     *
     * @param post   모집글 객체
     * @param user   사용자 객체
     * @param resume 이력서 객체
     * @param reason 지원 사유
     * @return 생성된 Apply 객체
     */
    public static Apply of(Post post, User user, Resume resume, String reason) {
        return Apply.builder()
                .post(post)
                .user(user)
                .resume(resume)
                .reason(reason)
                .build();
    }

    public void updateIsSelected(boolean selected) {
        this.isSelected = selected ? PassStatus.PASS : PassStatus.FAIL;
    }
}
