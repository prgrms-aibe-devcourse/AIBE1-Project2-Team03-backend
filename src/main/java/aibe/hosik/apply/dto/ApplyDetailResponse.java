package aibe.hosik.apply.dto;

import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.entity.PassStatus;
import aibe.hosik.post.entity.Post;
import aibe.hosik.profile.entity.Profile;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public record ApplyDetailResponse(
        Long applyId,

        Long userId,
        String introduction,
        String profileImage,

        // apply
        String reason,
        PassStatus isSelected,
        LocalDateTime applyAt,

        // resume
        Long resumeId,
        String title,
        String content,
        String personality,
        String portfolioUrl,
        List<String> skills,

        // ai
        Integer aiScore,
        String aiReason
) {
    public static ApplyDetailResponse from(Apply apply, List<String> skills, Analysis analysis) {
        User user = apply.getUser();
        Profile profile = user.getProfile();
        Resume resume = apply.getResume();
        Post post = apply.getPost();

        return new ApplyDetailResponse(
                apply.getId(),

                user.getId(),
                profile.getIntroduction(),
                profile.getImage(),

                apply.getReason(),
                apply.getIsSelected(),
                apply.getCreatedAt(),

                resume.getId(),
                resume.getTitle(),
                resume.getContent(),
                resume.getPersonality(),
                resume.getPortfolio(),
                skills,

                analysis != null ? analysis.getScore() : null,
                analysis != null ? analysis.getResult() : null
        );
    }
}
