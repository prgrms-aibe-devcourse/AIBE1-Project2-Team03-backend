package aibe.hosik.resume.dto;


import aibe.hosik.profile.dto.ProfileResponse;
import aibe.hosik.resume.entity.Resume;

import java.util.List;

public record ResumeDetailResponse(
        Long id,
        String title,
        String content,
        String personality,
        String portfolio,
        boolean isMain,
        List<String> skills,
        ProfileResponse profile
) {
    public static ResumeDetailResponse from(Resume resume) {
        return new ResumeDetailResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getContent(),
                resume.getPersonality(),
                resume.getPortfolio(),
                resume.isMain(),
                resume.getResumeSkills()
                        .stream()
                        .map(resumeSkill -> resumeSkill.getSkill().getName())
                        .toList(),
                ProfileResponse.from(resume.getUser().getProfile())
        );
    }
}
