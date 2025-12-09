package aibe.hosik.apply.dto;

import aibe.hosik.apply.entity.Apply;
import aibe.hosik.post.entity.Post;
import aibe.hosik.resume.entity.Resume;
import aibe.hosik.user.entity.User;

public record ApplyRequest(
        Long postId,
        Long resumeId,
        String reason
) {
    public Apply toEntity(Post post, User user, Resume resume) {
        return Apply.of(post, user, resume, reason());
    }
}
