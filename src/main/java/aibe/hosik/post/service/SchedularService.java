package aibe.hosik.post.service;


import aibe.hosik.analysis.repository.AnalysisRepository;
import aibe.hosik.analysis.service.AnalysisService;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedularService {
    private final PostRepository postRepository;
    private final ApplyRepository applyRepository;
    private final AnalysisRepository analysisRepository;
    private final AnalysisService analysisService;

    /**
     * 매일 자정 모집 기한 지난 글 idDone을 true로 설정
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkFinishedPosts() {
        log.info("모집글 기한 만료 체크");
        LocalDate today = LocalDate.now();

        // 마김되지 않은 게시글 중 마감일이 오늘까지 or 이전
        List<Post> finishedPost = postRepository.findByIsDoneFalseAndEndedAtLessThanEqual(today);

        for (Post post : finishedPost) {
            post.setDone(true);
            postRepository.save(post);
            log.info("모집글 ID: {} : 모집 기간 만료. 자동 마감 처리", post.getId());
        }
    }


    @Scheduled(cron = "0 0/30 * * * ?")
    @Transactional
    public void retryAnalysis() {
        log.info("AI 분석 실패 재시도");

        List<Apply> failedAnalysisApplies = applyRepository.findAppliesWithoutAnalysis();

        for (Apply apply : failedAnalysisApplies) {
            try {
                log.info("Apply ID {}에 대한 AI 분석 결과가 없어 재시도합니다.", apply.getId());
                analysisService.analysisApply(apply.getId());
            } catch (Exception e) {
                log.error("Apply ID {}에 대한 AI 분석 재시도 중 오류 발생", apply.getId(), e);
            }

        }
    }
}
