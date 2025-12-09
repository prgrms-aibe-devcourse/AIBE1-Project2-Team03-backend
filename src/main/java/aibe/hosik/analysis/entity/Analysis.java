package aibe.hosik.analysis.entity;

import aibe.hosik.apply.entity.Apply;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String result;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private int score;

    @ManyToOne(fetch = FetchType.LAZY)
    private Apply apply;

    public static Analysis of(Apply apply, String result, String summary, int score) {
        return Analysis.builder()
                .apply(apply)
                .result(result)
                .summary(summary)
                .score(score)
                .build();
    }
}
