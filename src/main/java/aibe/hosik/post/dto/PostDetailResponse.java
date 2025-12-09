package aibe.hosik.post.dto;

import aibe.hosik.post.entity.Post;

import java.time.LocalDate;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        Integer headCount,
        String image,
        String requirementPersonality,
        LocalDate endedAt,

        String category,
        String type,

        List<String> skills,

        // 현재 선택된 목록 보여주기
        List<MatchedUserResponse> matchedUsers,
        List<Long> applies,
        int currentCount,

        Long userId
) {
    public static PostDetailResponse from(Post post, List<String> skills, List<MatchedUserResponse> matchedUsers, int currentCount) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getHeadCount(),
                post.getImage(),
                post.getRequirementPersonality(),
                post.getEndedAt(),
                post.getCategory().toString(),
                post.getType().toString(),
                skills,
                matchedUsers,
                post.getApplies()
                        .stream()
                        .map(apply -> apply.getUser().getId()
                        ).toList(),
                currentCount,
                post.getUser().getId()
        );
    }
}
