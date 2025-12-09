package aibe.hosik.resume.dto;

import aibe.hosik.resume.entity.Resume;
import aibe.hosik.user.entity.User;

import java.util.List;

public record ResumeRequest(
        String title,
        String content,
        String personality,
        List<String> skills,
        boolean isMain
) {
    public Resume toEntity(String portfolio, User user) {
        return Resume.builder()
                .title(title)
                .content(content)
                .personality(personality)
                .portfolio(portfolio)
                .isMain(isMain)
                .user(user)
                .build();
    }
}