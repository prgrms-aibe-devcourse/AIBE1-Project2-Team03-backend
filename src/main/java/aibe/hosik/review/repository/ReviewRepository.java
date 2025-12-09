package aibe.hosik.review.repository;

import aibe.hosik.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByRevieweeId(Long userId);

    void deleteByIdAndReviewerId(Long id, Long reviewerId);
}
