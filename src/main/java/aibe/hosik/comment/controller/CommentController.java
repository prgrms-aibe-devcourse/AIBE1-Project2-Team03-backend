package aibe.hosik.comment.controller;

import aibe.hosik.comment.dto.CommentRequest;
import aibe.hosik.comment.dto.CommentResponse;
import aibe.hosik.comment.service.CommentService;
import aibe.hosik.handler.exception.CustomException;
import aibe.hosik.handler.exception.ErrorCode;
import aibe.hosik.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 API")
public class CommentController {
    private final CommentService commentService;

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "댓글 등록", description = "댓글 및 대댓글을 등록합니다.")
    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentRequest dto, @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        commentService.createComment(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "댓글 조회", description = " 모든 댓글 및 대댓글을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<CommentResponse>> getComments(@RequestParam Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        commentService.deleteComment(commentId, user);
        return ResponseEntity.noContent().build();
    }

    @SecurityRequirement(name = "JWT")
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    @PatchMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId,
                                           @RequestBody CommentRequest dto,
                                           @AuthenticationPrincipal User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.LOGIN_REQUIRED);
        }
        commentService.updateComment(commentId, dto, user);
        return ResponseEntity.ok().build();
    }
}
