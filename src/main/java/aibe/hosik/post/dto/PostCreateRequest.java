package aibe.hosik.post.dto;

import aibe.hosik.post.entity.Post;
import aibe.hosik.post.entity.PostCategory;
import aibe.hosik.post.entity.PostType;
import aibe.hosik.user.entity.User;

import java.time.LocalDate;
import java.util.List;

public record PostCreateRequest(
        String title,
        String content,
        Integer headCount,
        String requirementPersonality,
        LocalDate endedAt,

        PostCategory category,
        PostType type,

        List<String> skills
) {
    public Post toEntity(User user, String imageUrl) {
        return Post.builder()
                .title(title())
                .content(content())
                .headCount(headCount())
                .image(imageUrl)
                .requirementPersonality(requirementPersonality())
                .endedAt(endedAt())
                .category(category())
                .type(type())
                .user(user)
                .build();
    }
}
