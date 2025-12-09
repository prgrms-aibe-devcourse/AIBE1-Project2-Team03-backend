package aibe.hosik.comment.dto;

import aibe.hosik.comment.entity.Comment;
import aibe.hosik.profile.dto.ProfileResponse;
import aibe.hosik.user.entity.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record CommentResponse(
        Long id,
        String content,
        LocalDateTime createdAt,
        ProfileResponse profile,
        // 부모 댓글 > 자식 댓글
        List<CommentResponse> replies
) {
    public static CommentResponse from(Comment comment) {
        User user = comment.getUser();
        ProfileResponse profile = ProfileResponse.from(comment.getUser().getProfile());
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                profile,
                new ArrayList<>()
        );
    }
}


