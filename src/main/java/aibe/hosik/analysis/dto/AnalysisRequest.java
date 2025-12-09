package aibe.hosik.analysis.dto;

import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.apply.entity.Apply;

public record AnalysisRequest(
        Long id,
        String result,      // 분석 결과 (AI가 생성한 추천 이유)
        String summary,     // 자기소개서 요약
        int score,          // 최종 점수
        Long applyId        // 연결된 지원 ID
) {

    public static AnalysisRequest from(Analysis analysis) {
        return new AnalysisRequest(
                analysis.getId(),
                analysis.getResult(),
                analysis.getSummary(),
                analysis.getScore(),
                analysis.getApply().getId()
        );
    }

    public Analysis toEntity(Apply apply) {
        return Analysis.builder()
                .apply(apply)
                .result(this.result)
                .summary(this.summary)
                .score(this.score)
                .build();
    }

}