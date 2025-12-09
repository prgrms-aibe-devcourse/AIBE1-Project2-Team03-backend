package aibe.hosik.apply.service;

import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.analysis.repository.AnalysisRepository;
import aibe.hosik.analysis.service.AnalysisService;
import aibe.hosik.apply.dto.ApplyByResumeSkillResponse;
import aibe.hosik.apply.dto.ApplyDetailResponse;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.entity.PassStatus;
import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.resume.repository.ResumeRepository;
import aibe.hosik.skill.entity.ResumeSkill;
import aibe.hosik.skill.repository.ResumeSkillRepository;
import aibe.hosik.user.entity.User;
import aibe.hosik.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplyService {

    private final ApplyRepository applyRepository; // Apply 테이블과 통신하는 레포
    private final PostRepository postRepository; // Post 테이블과 통신
    private final ResumeRepository resumeRepository; // Resume 테이블과 통신
    private final UserRepository userRepository; // User 테이블과 통신
    private final ResumeSkillRepository resumeSkillRepository;
    private final AnalysisRepository analysisRepository;
    private final AnalysisService analysisService;


    /**
     * 사용자가 특정 모집글에 특정 이력서를 가지고 지원하는 기능
     *
     * @param userId   지원자 ID
     * @param postId   모집글 ID
     * @param resumeId 지원자가 선택한 이력서 ID
     */
    public void apply(Long userId, Long postId, Long resumeId, String reason) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        if (!resume.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.RESUME_FORBIDDEN);
        }

        Apply apply = Apply.of(post, user, resume, reason);
        applyRepository.save(apply);

        log.info("AI 분석 시작 - applyId: {}", apply.getId());
        try {
            analysisService.analysisApply(apply.getId());

        } catch (Exception e) {
            log.error("AI 분석 중 오류 발생", e);
        }
    }

    /**
     * 지원서 모아보기
     * 지정된 구인 공고 ID에 연결된 지원 데이터를 기반으로, 지원 정보와 이력서에 포함된 스킬 정보를 함께 반환합니다.
     *
     * @param postId 대상 구인 공고 ID
     * @return ApplyByResumeSkillResponse 객체의 리스트. 각 객체는 지원 정보 및 해당 지원자의 이력서에 포함된 스킬 정보를 포함합니다.
     */
    public List<ApplyByResumeSkillResponse> getApplyResumeWithSkillsByPostId(Long postId, User user) {
        // 모집글 정보 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        // 모집글 작성자 검증
        if (!post.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.POST_AUTHOR_FORBIDDEN);
        }

        List<Apply> applies = applyRepository.findWithUserResumeAndAnalysisByPostId(postId);

        return applies.stream()
                .map(apply -> {
                    // 이력서에 연결된 스킬 정보 조회
                    List<String> skills = getSkillsByResumeId(apply.getResume().getId());
                    Analysis analysis = analysisRepository.findLatestByApplyId(apply.getId()).orElse(null);

                    // 정적 팩토리 메서드 활용
                    return ApplyByResumeSkillResponse.from(apply, skills, analysis);
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 모집글에 지원한 사람들의 자기소개서 전문을 반환하는 기능
     * 게시글 상세보기 기능
     */
    public ApplyDetailResponse getApplyDetailByApplyId(Long applyId, User user) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_APPLY));

        if (!apply.getPost().getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.POST_AUTHOR_FORBIDDEN);
        }

        List<String> skills = getSkillsByResumeId(apply.getResume().getId());
        Analysis analysis = analysisRepository.findLatestByApplyId(applyId).orElse(null);

        return ApplyDetailResponse.from(apply, skills, analysis);
    }


    /**
     * 주어진 지원서 ID에 해당하는 지원서를 삭제합니다.
     *
     * @param applyId 삭제하려는 지원서의 식별자(ID)
     * @param user    현재 요청을 수행하는 사용자
     */
    public void deleteApply(Long applyId, User user) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_APPLY));

        if (!apply.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.APPLY_DELETE_FORBIDDEN);
        }
        applyRepository.delete(apply);
    }

    /**
     * 특정 지원서의 매칭 선택 여부를 업데이트하는 메서드.
     *
     * @param applyId    선택 여부를 업데이트할 지원서의 ID
     * @param isselected 선택 여부 상태 (true: 선택됨, false: 선택되지 않음)
     * @param user       현재 요청을 보낸 사용자 정보
     * @throws ResponseStatusException 지원서가 존재하지 않거나, 모집글 작성자가 아닌 경우 예외 발생
     */
    public void updateIsSelected(Long applyId, boolean isselected, User user) {
        Apply apply = applyRepository.findById(applyId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_APPLY));

        Post post = apply.getPost();

        if (!apply.getPost().getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.POST_AUTHOR_FORBIDDEN);
        }

        PassStatus previous = apply.getIsSelected();

        // 선택 업데이트
        apply.updateIsSelected(isselected);
        applyRepository.save(apply);

        int currentCount = applyRepository.countByPostIdAndIsSelected(post.getId(), PassStatus.PASS);

        // 매칭 선택 했을 때
        if (previous.equals(PassStatus.FAIL) && isselected) {
            if (currentCount >= post.getHeadCount()) {
                post.setDone(true);
                postRepository.save(post);
            }
        }

        // 매칭 취소 시 다시 모집 중 전환
        else if (previous.equals(PassStatus.PASS) && !isselected) {
            if (post.isDone() && currentCount < post.getHeadCount()) {
                post.setDone(false);
                postRepository.save(post);
            }
        }
    }


    /**
     * 이력서 ID로 해당 이력서에 연결된 모든 스킬 이름을 조회하는 내부 메서드
     *
     * @param resumeId 이력서 ID
     * @return 스킬 이름 목록
     */
    private List<String> getSkillsByResumeId(Long resumeId) {
        List<ResumeSkill> resumeSkills = resumeSkillRepository.findByResumeId(resumeId);
        return resumeSkills.stream()
                .map(rs -> rs.getSkill().getName())
                .collect(Collectors.toList());
    }
}