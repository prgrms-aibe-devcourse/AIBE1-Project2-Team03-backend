package aibe.hosik.review.dto;

public record ReviewRequest(
        Long postId,
        String content
) {
}
