package aibe.hosik.apply.dto;

import aibe.hosik.analysis.entity.Analysis;
import aibe.hosik.apply.entity.Apply;
import aibe.hosik.apply.entity.PassStatus;
import aibe.hosik.profile.entity.Profile;
import aibe.hosik.resume.dto.ResumeDetailResponse;
import aibe.hosik.user.entity.User;
import lombok.Builder;

import java.util.List;

@Builder
public record ApplyByResumeSkillResponse(
        Long applyId,
        Long userId,
        ResumeDetailResponse resume,
        String nickname,
        String profileImage,
        PassStatus isSelected,
        String reason,

        Integer aiScore,
        String aiReason,
        String aiSummary
) {
    public static ApplyByResumeSkillResponse from(Apply apply, List<String> skills, Analysis analysis) {
        User user = apply.getUser();
        Profile profile = user.getProfile();
        ResumeDetailResponse resume = ResumeDetailResponse.from(apply.getResume());

        return ApplyByResumeSkillResponse.builder()
                .applyId(apply.getId())
                .userId(user.getId())
                .resume(resume)
                .nickname(profile.getNickname())
                .profileImage(profile.getImage())
                .isSelected(apply.getIsSelected())
                .reason(apply.getReason())
                .aiScore(analysis != null ? analysis.getScore() : null)
                .aiReason(analysis != null ? analysis.getResult() : null)
                .aiSummary(analysis != null ? analysis.getSummary() : null)
                .build();
    }
}
