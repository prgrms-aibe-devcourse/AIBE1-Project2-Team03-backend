package aibe.hosik.comment.service;

import aibe.hosik.comment.dto.CommentRequest;
import aibe.hosik.comment.dto.CommentResponse;
import aibe.hosik.comment.entity.Comment;
import aibe.hosik.comment.repository.CommentRepository;
import aibe.hosik.post.entity.Post;
import aibe.hosik.post.repository.PostRepository;
import aibe.hosik.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    /**
     * 새로운 댓글을 생성합니다.
     *
     * @param dto  댓글 작성 정보를 담고 있는 DTO 객체
     * @param user 댓글을 작성하는 사용자 정보
     */
    @Transactional
    public void createComment(CommentRequest dto, User user) {
        // post id로 게시글 찾기
        Post post = postRepository.findById(dto.postId())
                .orElseThrow();

        // 대댓글일 경우 부모 댓글 확인
        Comment parent = null;
        if (dto.parentCommentId() != null) {
            parent = commentRepository.findById(dto.parentCommentId())
                    .orElseThrow();
        }

        // 댓글 생성 - 엔티티 변환
        Comment comment = dto.toEntity(post, user, parent);
        commentRepository.save(comment);
    }

    /**
     * 지정된 게시글 ID에 속한 모든 댓글을 검색하고, 부모-자식 관계에 따라 데이터를 계층적으로 정리하여 반환합니다.
     *
     * @param postId 댓글을 조회하고자 하는 게시글의 ID
     * @return 지정된 게시글 ID에 속하는 모든 부모 댓글이 포함된 리스트. 각 댓글은 자식 댓글을 포함하여 계층적으로 표현됨.
     */
    @Transactional
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // postId의 댓글 모두 찾기
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        Map<Long, CommentResponse> commentMap = new HashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        // 댓글 DTO 변환 > Map에 저장
        for (Comment c : comments) {
            // DTO 변환
            CommentResponse dto = CommentResponse.from(c);
            commentMap.put(c.getId(), dto);

            if (c.getParentComment() == null) {
                roots.add(dto);
            } else {
                // 부모가 있으면 부모의 replies에 추가
                CommentResponse parentDto = commentMap.get(c.getParentComment().getId());
                parentDto.replies().add(dto);
            }
        }
        return roots;
    }

    /**
     * 특정 댓글을 삭제합니다. 삭제하려는 댓글이 현재 사용자가 작성한 댓글인지 확인하는 과정을 포함합니다.
     *
     * @param commentId 삭제할 댓글의 ID
     * @param user      요청을 수행하는 사용자 정보
     * @throws ResponseStatusException 댓글 작성자가 아닌 경우 삭제가 허용되지 않음
     */
    @Transactional
    public void deleteComment(Long commentId, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow();

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 삭제할 수 있습니다");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void updateComment(Long commentId, CommentRequest dto, User user) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow();

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "댓글 작성자만 수정할 수 있습니다");
        }
        comment.updateContent(dto.content());
    }
}
