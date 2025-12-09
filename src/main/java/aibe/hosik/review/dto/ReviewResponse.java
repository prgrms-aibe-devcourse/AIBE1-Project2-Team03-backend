package aibe.hosik.review.dto;

import aibe.hosik.post.entity.Post;
import aibe.hosik.profile.dto.ProfileResponse;
import aibe.hosik.review.entity.Review;

public record ReviewResponse(
        Long id,
        String content,
        ProfileResponse reviewer,
        Long postId,
        String postTitle
) {

    public static ReviewResponse from(Review review) {
        ProfileResponse profile = ProfileResponse.from(review.getReviewer().getProfile());
        Post post = review.getPost();
        return new ReviewResponse(
                review.getId(),
                review.getContent(),
                profile,
                post == null ? null : post.getId(),
                post == null ? null : post.getTitle()
        );
    }
}
