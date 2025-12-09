package aibe.hosik.review.service;

import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.review.dto.ReviewRequest;
import aibe.hosik.review.dto.ReviewResponse;
import aibe.hosik.review.entity.Review;
import aibe.hosik.review.repository.ReviewRepository;
import aibe.hosik.user.entity.User;
import aibe.hosik.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void createReview(ReviewRequest request, Long revieweeId, User reviewer) {
        User reviewee = userRepository.findById(revieweeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        Post post = request.postId() == null
                ? null
                : postRepository.findById(request.postId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .content(request.content())
                .post(post)
                .build();

        reviewRepository.save(review);
    }

    public List<ReviewResponse> getAllReviewsByUserId(Long userId) {
        return reviewRepository.findAllByRevieweeId(userId)
                .stream()
                .map(ReviewResponse::from)
                .toList();
    }

    public ReviewResponse getReviewDetail(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_REVIEW));
        return ReviewResponse.from(review);
    }

    public void updateReview(ReviewRequest request, Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_REVIEW));

        Post post = request.postId() == null
                ? review.getPost()
                : postRepository.findById(request.postId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_POST));

        review = review.toBuilder()
                .content(request.content())
                .post(post)
                .build();

        reviewRepository.save(review);
    }

    public void deleteReview(Long reviewId, User user) {
        reviewRepository.deleteByIdAndReviewerId(reviewId, user.getId());
    }
}
