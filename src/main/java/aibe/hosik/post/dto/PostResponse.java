package aibe.hosik.post.dto;

import aibe.hosik.post.entity.Post;

import java.time.LocalDate;
import java.util.List;

public record PostResponse(
        Long id,
        String image,
        String title,
        String content,
        String category,
        String type,
        LocalDate endedAt,
        List<String> skills,
        Integer headCount,
        Integer currentCount
) {

    public static PostResponse from(Post post, List<String> skills, Integer currentCount) {
        return new PostResponse(post.getId(),
                post.getImage(),
                post.getTitle(),
                post.getContent(),
                post.getCategory().toString(),
                post.getType().toString(),
                post.getEndedAt(),
                skills,
                post.getHeadCount(),
                currentCount
        );
    }
}
