package aibe.hosik.analysis.controller;

import aibe.hosik.analysis.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "분석 API") // Swagger Tag
public class AnalysisController {
    private final AnalysisService analysisService;

    /**
     * 수동으로 AI 분석을 재실행하는 엔드포인트
     *
     * @param applyId 지원서 ID
     * @return 성공 또는 실패 메시지
     */
    @Operation(
            summary = "AI 분석 수동 재시도",
            description = "지원서 ID를 기반으로 AI 분석을 수동으로 재실행"
    )
    @PostMapping("/retry/{applyId}")
    public ResponseEntity<String> retryAnalysis(@PathVariable Long applyId) {
        try {
            analysisService.analysisApply(applyId);  // 동기 실행
            return ResponseEntity.ok("AI 분석이 성공적으로 완료");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("AI 분석 중 오류 발생: " + e.getMessage());
        }
    }
}
