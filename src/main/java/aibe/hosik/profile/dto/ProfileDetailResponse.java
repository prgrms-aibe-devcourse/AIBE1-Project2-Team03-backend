package aibe.hosik.profile.dto;

import aibe.hosik.post.dto.PostResponse;
import aibe.hosik.resume.dto.ResumeDetailResponse;
import aibe.hosik.review.dto.ReviewResponse;

import java.util.List;

public record ProfileDetailResponse(
        ProfileResponse profile,
        List<PostResponse> authorPosts,
        List<PostResponse> joinedPosts,
        List<ReviewResponse> reviews,
        List<ResumeDetailResponse> resumes
) {
    public static ProfileDetailResponse from(
            ProfileResponse profile,
            List<PostResponse> authorPosts,
            List<PostResponse> joinedPosts,
            List<ReviewResponse> reviews,
            List<ResumeDetailResponse> resumes
    ) {
        return new ProfileDetailResponse(profile, authorPosts, joinedPosts, reviews, resumes);
    }
}
