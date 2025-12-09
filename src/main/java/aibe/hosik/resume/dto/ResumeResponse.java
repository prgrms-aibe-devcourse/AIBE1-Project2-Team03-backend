package aibe.hosik.resume.dto;

import aibe.hosik.profile.dto.ProfileResponse;
import aibe.hosik.resume.entity.Resume;
import lombok.Builder;

import java.util.List;

@Builder
public record ResumeResponse(
        Long id,
        String title,
        String content,
        List<String> skills,
        ProfileResponse profile
) {
    public static ResumeResponse from(Resume resume) {
        List<String> skills = resume.getResumeSkills()
                .stream()
                .map(resumeSkill -> resumeSkill.getSkill().getName())
                .toList();
        ProfileResponse profile = ProfileResponse.from(resume.getUser().getProfile());

        return ResumeResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .content(resume.getContent())
                .skills(skills)
                .profile(profile)
                .build();
    }
}
