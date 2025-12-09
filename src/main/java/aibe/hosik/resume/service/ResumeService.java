package aibe.hosik.resume.service;

import aibe.hosik.apply.repository.ApplyRepository;
import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.post.service.StorageService;
import aibe.hosik.resume.dto.ResumeDetailResponse;
import aibe.hosik.resume.dto.ResumeRequest;
import aibe.hosik.resume.dto.ResumeResponse;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.resume.repository.ResumeRepository;
import aibe.hosik.skill.entity.ResumeSkill;
import aibe.hosik.skill.entity.Skill;
import aibe.hosik.skill.repository.ResumeSkillRepository;
import aibe.hosik.skill.repository.SkillRepository;
import aibe.hosik.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final SkillRepository skillRepository;
    private final ApplyRepository applyRepository;

    private final StorageService storageService;

    public void createResume(ResumeRequest request, MultipartFile file, User user) {
        if (request.isMain()) {
            resumeRepository.resetMainResumeFlag(user.getId());
        }

        String portfolio = file == null ? null : storageService.upload(file);
        Resume resume = request.toEntity(portfolio, user);
        Resume saved = resumeRepository.save(resume);

        List<String> skills = new ArrayList<>();

        for (String skillName : request.skills()) {
            Skill skill = skillRepository.findByName(skillName)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));

            ResumeSkill resumeSkill = ResumeSkill.builder()
                    .resume(saved)
                    .skill(skill)
                    .build();

            // post-skill 연관관계 추가
            resumeSkillRepository.save(resumeSkill);
            // 응답 DTO 스킬 저장
            skills.add(skill.getName());
        }

        resumeRepository.save(resume);
    }

    public ResumeDetailResponse getResume(Long id) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RESUME));
        return ResumeDetailResponse.from(resume);
    }

    public List<ResumeDetailResponse> getAllResumesByUserId(Long userId) {
        return resumeRepository.findAllByUserId(userId)
                .stream()
                .map(ResumeDetailResponse::from)
                .toList();
    }

    public List<ResumeResponse> getAllMainResumes() {
        return resumeRepository.findAllMainResumes()
                .stream()
                .map(ResumeResponse::from)
                .toList();
    }

    public void updateResume(Long resumeId, ResumeRequest request, MultipartFile file, User user) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RESUME));
        resumeRepository.resetMainResumeFlag(user.getId());

        String portfolio = file == null ? resume.getPortfolio() : storageService.upload(file);

        List<String> skills = new ArrayList<>();

        for (String skillName : request.skills()) {
            Skill skill = skillRepository.findByName(skillName)
                    .orElseGet(() -> skillRepository.save(Skill.builder().name(skillName).build()));

            ResumeSkill resumeSkill = ResumeSkill.builder()
                    .resume(resume)
                    .skill(skill)
                    .build();

            // post-skill 연관관계 추가
            resumeSkillRepository.save(resumeSkill);
            // 응답 DTO 스킬 저장
            skills.add(skill.getName());
        }

        Resume updated = resume.toBuilder()
                .title(request.title())
                .content(request.content())
                .personality(request.personality())
                .portfolio(portfolio)
                .isMain(request.isMain())
                .build();

        resumeRepository.save(updated);
    }

    public void deleteResume(Long resumeId, User user) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RESUME));

        resumeRepository.delete(resume);
    }
}