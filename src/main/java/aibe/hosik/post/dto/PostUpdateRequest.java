package aibe.hosik.post.dto;

import aibe.hosik.post.entity.PostCategory;
import aibe.hosik.post.entity.PostType;

import java.time.LocalDate;
import java.util.List;

public record PostUpdateRequest(
        String title,
        String content,
        Integer headCount,
        String requirementPersonality,
        LocalDate endedAt,

        PostCategory category,
        PostType type,

        List<String> skills
) {
}
