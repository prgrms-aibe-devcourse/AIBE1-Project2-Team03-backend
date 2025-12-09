package aibe.hosik.analysis.service;

import aibe.hosik.analysis.client.GeminiClient;
import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.analysis.repository.AnalysisRepository;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.post.entity.Post;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.skill.entity.ResumeSkill;
import aibe.hosik.skill.repository.PostSkillRepository;
import aibe.hosik.skill.repository.ResumeSkillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final GeminiClient geminiClient;
    private final AnalysisRepository analysisRepository;
    private final PostSkillRepository postSkillRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ApplyRepository applyRepository;

    @Async
    public CompletableFuture<Void> analysisApplyAsync(Long applyId) {
        return CompletableFuture.runAsync(() -> {
            try {
                analysisApply(applyId);
                log.info("Apply ID {}에 대한 AI 분석이 완료되었습니다.", applyId);
            } catch (Exception e) {
                log.error("Apply ID {}에 대한 AI 분석 중 오류 발생", applyId, e);
            }
        });
    }

    public Analysis analysisApply(Long applyId) {

        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "지원서를 찾을 수 없습니다."));

        long startTotal = System.currentTimeMillis();

        // 강제 재실행시 테스트
        Optional<Analysis> existing = analysisRepository.findLatestByApplyId(applyId);

        Resume resume = apply.getResume();
        Post post = apply.getPost();

        // 스킬 목록 조회
        List<String> postSkillNames = getPostSkillNames(post.getId());
        List<String> resumeSkillNames = getResumeSkillNames(resume.getId());

        // 성격 조회
        String postRequirementPersonality = post.getRequirementPersonality();
        String resumePersonality = resume.getPersonality();

        log.info("AI 분석 시작 - applyId: {}", applyId);
        Instant start = Instant.now();
        try {
            // 병렬처리
            // 모델 1, 2 병렬 분석
            CompletableFuture<String> analysisModel1Future = analysisModel1(post, resume, apply,
                    postRequirementPersonality, postSkillNames, resumePersonality, resumeSkillNames);
            CompletableFuture<String> analysisModel2Future = analysisModel2(post, resume, apply,
                    postRequirementPersonality, postSkillNames, resumePersonality, resumeSkillNames);

            // 모델 3
            CompletableFuture<String> summarizeResumeFuture = analysisModel3(resume);

            // 모든 분석 대기
            CompletableFuture.allOf(analysisModel1Future, analysisModel2Future, summarizeResumeFuture).join();

            // 모델 1, 2, 3 결과 가져오기
            String model1Result = analysisModel1Future.join();
            String model2Result = analysisModel2Future.join();
            String resumeSummary = summarizeResumeFuture.join();

            // 모델 4로 최종 점수 결과 생성
            CompletableFuture<String> finalAnalysisFuture = analysisModel4(model1Result, model2Result);
            String finalAnalysisResult = finalAnalysisFuture.join();
            log.info(finalAnalysisResult);

            // 점수 추출
            int finalScore = extractScoreFromAnalysis(finalAnalysisResult);

            // AI 결과 저장
            Analysis analysis;
            if (existing.isPresent()) {
                // 덮어쓰기
                analysis = existing.get();
                analysis.setResult(finalAnalysisResult);
                analysis.setScore(finalScore);
                analysis.setSummary(resumeSummary);
            } else {
                // 새로 생성
                analysis = Analysis.builder()
                        .apply(apply)
                        .result(finalAnalysisResult)
                        .summary(resumeSummary)
                        .score(finalScore)
                        .build();
            }

            Instant end = Instant.now();  // ⏱ 종료 시간 기록
            long durationMs = Duration.between(start, end).toMillis();
            log.info("AI 전체 분석 완료 - applyId: {}, 소요 시간: {} ms", applyId, durationMs);

            return analysisRepository.save(analysis);
        } catch (Exception e) {
            log.error("Apply ID {}에 대한 AI 분석 중 오류 발생", applyId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "AI 분석 중 오류가 발생했습니다.", e);
        }
    }


    private CompletableFuture<String> analysisModel1(
            Post post, Resume resume, Apply apply,
            String postRequirementPersonality, List<String> postSkillNames, String resumePersonality, List<String> resumeSkillNames) {
        String prompt = String.format("""
                        당신은 지원서를 보고 모집글에 가장 적합한 지원서를 점수화하고, 지원자를 선정하는데 최적화된 AI 어시스턴트입니다.
                        모집글의 정보는 다음과 같습니다.
                        [모집글 정보]
                        - 모집 내용 : %s
                        - 요구사항 및 요구성격, 우대사항 : %s
                        - 필요 기술 스킬 : %s
                        
                        지원자 정보는 다음과 같습니다.
                        [지원자 정보]
                        - 이력서 : %s 
                        - 지원 동기 : %s
                        - 성격 및 특징 : %s
                        - 보유하고 있는 기술 스킬 : %s
                        
                        분석에 사용할 기준은 다음과 같습니다.
                        [기준]
                        - 내용 적합도 점수는 [모집글 정보]의 "모집 내용"과, [지원자 정보]의 "이력서", "지원 동기" 파트를 비교하여 지원자가 모집글에 어울리는 사람인지 판단해주세요.
                        - 성격 점수는 [모집글 정보]의 "요구사항 및 요구성격, 우대사항"과 [지원자 정보]의 "성격 및 특징"을 비교하고 분석하여 두 사람이 성격적으로 잘 맞을지 판단해주세요.
                        - 스킬 점수는 [모집글 정보]의 "필요 기술 스킬"과 [지원자 정보]의 "보유하고 있는 기술 스킬"의 일치도를 바탕으로 나타내주세요. 모두 일치할 경우 100. 그외에는 비슷한 스킬의 경우일 경우 점수 부여.
                        
                        위의 [기준]을 바탕으로 다음과 같은 형식으로 분석 결과를 제공해주세요. 숫자의 크기가 클수록 일치도가 높고 적합한 사람입니다
                        [결과 형식]
                        - 내용 적합도 점수 : 0-100 사이의 숫자
                        - 성격 점수 : 0-100 사이의 숫자
                        - 스킬 점수 : 0-100 사이의 숫자
                        - 이유 : 내용 적합도, 성격, 스킬을 바탕으로 점수 선정 이유와 추천 이유를 400자 이내로 작성해주세요.
                        """,
                post.getContent(), postRequirementPersonality, postSkillNames, resume.getContent(), apply.getReason(), resumePersonality, resumeSkillNames);

        CompletableFuture<String> responseFuture = geminiClient.analysisWithModel1(prompt);
        log.info("모델 1 프롬프트 전송");

        return responseFuture.thenApply(response -> {
            log.info("모델 1 응답 받음: {}", response);
            return response;
        });
    }

    private CompletableFuture<String> analysisModel2(
            Post post, Resume resume, Apply apply,
            String postRequirementPersonality, List<String> postSkillNames, String resumePersonality, List<String> resumeSkillNames) {
        String prompt = String.format("""
                        당신은 지원서를 보고 모집글에 가장 적합한 지원서를 점수화하고, 지원자를 선정하는데 최적화된 AI 어시스턴트입니다.
                        모집글의 정보는 다음과 같습니다.
                        [모집글 정보]
                        - 모집 내용 : %s
                        - 요구사항 및 요구성격, 우대사항 : %s
                        - 필요 기술 스킬 : %s
                        
                        지원자 정보는 다음과 같습니다.
                        [지원자 정보]
                        - 이력서 : %s
                        - 지원 동기 : %s
                        - 성격 및 특징 : %s
                        - 보유하고 있는 기술 스킬 : %s
                        
                        분석에 사용할 기준은 다음과 같습니다.
                        [기준]
                        - 내용 적합도 점수는 [모집글 정보]의 "모집 내용"과, [지원자 정보]의 "이력서", "지원 동기" 파트를 비교하여 지원자가 모집글에 어울리는 사람인지 판단해주세요.
                        - 성격 점수는 [모집글 정보]의 "요구사항 및 요구성격, 우대사항"과 [지원자 정보]의 "성격 및 특징"을 비교하고 분석하여 두 사람이 성격적으로 잘 맞을지 판단해주세요.
                        - 스킬 점수는 [모집글 정보]의 "필요 기술 스킬"과 [지원자 정보]의 "보유하고 있는 기술 스킬"의 일치도를 바탕으로 나타내주세요. 모두 일치할 경우 100. 그외에는 비슷한 스킬의 경우일 경우 점수 부여.
                        
                        위의 [기준]을 바탕으로 다음과 같은 형식으로 분석 결과를 제공해주세요. 숫자의 크기가 클수록 일치도가 높고 적합한 사람입니다
                        [결과 형식]
                        - 내용 적합도 점수 : 0-100 사이의 숫자
                        - 성격 점수 : 0-100 사이의 숫자
                        - 스킬 점수 : 0-100 사이의 숫자
                        - 이유 : 내용 적합도, 성격, 스킬을 바탕으로 점수 선정 이유와 추천 이유를 400자 이내로 작성해주세요.
                        """,
                post.getContent(), postRequirementPersonality, postSkillNames, resume.getContent(), apply.getReason(), resumePersonality, resumeSkillNames);

        CompletableFuture<String> responseFuture = geminiClient.analysisWithModel2(prompt);
        log.info("모델 2 프롬프트 전송");

        return responseFuture.thenApply(response -> {
            log.info("모델 2 응답 받음: {}", response);
            return response;
        });
    }

    private CompletableFuture<String> analysisModel3(Resume resume) {
        // 자기소개서 내용이 없을 경우 기본 메시지 반환
        if (resume.getContent() == null || resume.getContent().trim().isEmpty()) {
            return CompletableFuture.completedFuture("자기소개서 내용이 없습니다.");
        }
        String prompt = String.format("""
                다음은 지원자의 자기소개서입니다. 핵심 내용을 200자 이내로 간결하게 요약해주세요:
                
                %s
                """, resume.getContent());
        return geminiClient.summarizeResume(prompt);
    }

    private CompletableFuture<String> analysisModel4(String model1Result, String model2Result) {
        String prompt = String.format("""
                당신은 지원서 분석 결과를 종합하여 최종 평가를 내리는 AI 어시스턴트입니다.
                다음은 두 개의 다른 모델이 동일한 지원서에 대해 분석한 결과입니다:
                
                [모델 1 분석 결과]
                %s
                
                [모델 2 분석 결과]
                %s
                
                위 두 모델의 분석 결과를 종합하여 다음 형식으로 추천 평가를 제공해주세요. 다른 추가적인 부분은 모두 제외하고 아래 형식을 지켜 작성해주세요.:
                
                - 추천 점수: 0-100 사이의 숫자 (두 모델의 내용 적합도 점수, 성격 점수와 스킬 점수를 종합적으로 고려, 숫자가 클수록 추천도가 높음)
                - 추천 이유: 두 모델을 종합한 [내용 적합도 점수: , 성격 점수: , 스킬 점수: ]를 작성해주시고, 
                두 모델이 제공한 이유를 바탕으로 이 지원자가 해당 모집글에 왜 적합한지, 위의 점수가 나온 이유 및 추천 이유를 400자 이내로 작성해주세요.
                이때 "모델의 결과를 종합한" "모델 결과"와 같은 말은 빼고, 사용자 친화적으로 AI 어시스턴트가 추천하는 말을 한글로 작성해주세요.
                """, model1Result, model2Result);

        return geminiClient.generateFinalAnalysis(prompt);
    }

    // 점수 추출 메서드
    private int extractScoreFromAnalysis(String analysisResult) {
        try {
            String scoreText = analysisResult.split("추천 점수:")[1].trim().split("\\s+")[0];
            // 숫자만 추출
            return Integer.parseInt(scoreText.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            log.error("점수 추출 중 오류 발생", e);
            // 오류 발생 시 기본값 반환
            return 0;
        }
    }

    /**
     * 모집글 ID로 스킬 이름 목록을 조회합니다.
     */
    private List<String> getPostSkillNames(Long postId) {
        // 기존 코드를 수정: PostSkillRepository의 findSkillByPostId 메서드 사용
        return postSkillRepository.findSkillByPostId(postId);
    }

    /**
     * 이력서 ID로 스킬 이름 목록을 조회합니다.
     */
    private List<String> getResumeSkillNames(Long resumeId) {
        // 기존 코드를 수정: ResumeSkillRepository의 findByResumeId 메서드 사용 후 스킬 이름만 추출
        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resumeId);
        return resumeSkills.stream()
                .map(rs -> rs.getSkill().getName())
                .collect(Collectors.toList());
    }
}
